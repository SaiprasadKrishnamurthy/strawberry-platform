package com.sai.strawberry.micro;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Predicates;
import com.mongodb.MongoClient;
import com.sai.strawberry.api.EventStreamConfig;
import com.sai.strawberry.micro.actor.ESPercolationSetupActor;
import com.sai.strawberry.micro.actor.KibanaEngineDashboardSetupActor;
import com.sai.strawberry.micro.actor.RepositoryActor;
import com.sai.strawberry.micro.config.ActorFactory;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import org.apache.commons.io.IOUtils;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Created by saipkri on 07/09/16.
 */
@SpringBootApplication
@EnableMongoRepositories
//@EnableEurekaClient
@EnableSwagger2
@EnableKafka
public class EventProcessorApplication {

    private static final Logger LOGGER = Logger.getLogger(EventProcessorApplication.class);

    @Value("${mongoHost}")
    private String mongoHost;

    @Value("${mongoPort}")
    private int mongoPort;

    @Value("${esUrl}")
    private String esUrl;

    @Value("${kafkaBrokersCsv}")
    private String kafkaBrokersCsv;

    @Value("${mongoDb}")
    private String mongoDb;

    @Value("${zookeeperUrls}")
    private String zookeeperUrls;

    @Value("${mongoDbForBatch}")
    private String mongoDbForBatch;

    @Value("${esIndexBatchSize}")
    private int esIndexBatchSize;

    @Value("classpath:*.json")
    private Resource[] configs;

    @Inject
    private ApplicationContext applicationContext;

    @Value("${cleanupExistingIndex ?: true}")
    private boolean cleanupExistingIndex;

    @Value("${kibanaOpsIndexName ?: strawberryOpsIdx}")
    private String kibanaOpsIndexName;


    private ActorSystem actorSystem() {
        return ActorSystem.create("StrawberryActorSystem");
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer properties() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public ActorFactory actorFactory(final MongoTemplate mongoTemplate) throws Exception {
        ActorFactory actorFactory = new ActorFactory(actorSystem(), kafkaProducer(), jestClient(), mongoTemplate, mongoForBatch(), esIndexBatchSize, esUrl, kibanaOpsIndexName.toLowerCase().trim());
        initConfigs(actorFactory);
        return actorFactory;
    }

    @Bean
    public MongoTemplate mongoTemplate(final MongoDbFactory mongoDbFactory, final MappingMongoConverter mappingMongoConverter) {
        MongoTemplate mongoTemplate = new MongoTemplate(mongoDbFactory, mappingMongoConverter);
        return mongoTemplate;
    }

    private void initConfigs(final ActorFactory actorFactory) {
        final ObjectMapper jsonParser = new ObjectMapper();
        System.out.println("\n\n\n------------- INITIALIZING CONFIGSS --------------------");
        Stream.of(configs)
                .map(resource -> {
                    try {
                        System.out.println(" ******** " + resource.getFilename());
                        return Optional.of(jsonParser.readValue(IOUtils.toString(resource.getInputStream()), EventStreamConfig.class));
                    } catch (IOException e) {
                        LOGGER.info("Didn't load "+resource+" in config db. Doesn't appear to be a config file.");
                        return Optional.empty();
                    }
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(config -> {
                    ActorRef repoActor = actorFactory.newActor(RepositoryActor.class);
                    repoActor.tell(config, ActorRef.noSender());

                    // Setup the percolation.
                    ActorRef esPercolationSetupActor = actorFactory.newActor(ESPercolationSetupActor.class);
                    esPercolationSetupActor.tell(Arrays.asList(cleanupExistingIndex, config), ActorRef.noSender());

                    // Setup the ops dashboard.
                    ActorRef opsDashboard = actorFactory.newActor(KibanaEngineDashboardSetupActor.class);
                    opsDashboard.tell(kibanaOpsIndexName, ActorRef.noSender());

                });
    }

    @Bean
    public MongoDbFactory getMongoDbFactory() throws Exception {
        MongoClient mongoClient = new MongoClient(mongoHost, mongoPort);
        return new SimpleMongoDbFactory(mongoClient, mongoDb);
    }

    @Bean
    public MappingMongoConverter mappingMongoConvertor(MongoDbFactory mongoDbFactory) {
        MappingMongoConverter mappingMongoConverter = new MappingMongoConverter(mongoDbFactory, new MongoMappingContext());
        mappingMongoConverter.setMapKeyDotReplacement("##");
        return mappingMongoConverter;
    }

    @Bean
    @Qualifier("batchDb")
    public MongoTemplate mongoForBatch() throws Exception {
        MongoClient mongoClient = new MongoClient(mongoHost, mongoPort);
        MongoDbFactory mongoDbFactory = new SimpleMongoDbFactory(mongoClient, mongoDbForBatch);
        return new MongoTemplate(mongoDbFactory);
    }

    @Bean
    public KafkaProducer<String, String> kafkaProducer() {
        return new KafkaProducer<>(senderProps());
    }

    private Map<String, Object> senderProps() {
        System.out.println("\n\n\n " + kafkaBrokersCsv);
        // OK to hard code for now. May be to move it to appProperties later.
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBrokersCsv);
        props.put(ProducerConfig.RETRIES_CONFIG, 0);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        return props;
    }

    /**
     * Swagger 2 docket bean configuration.
     *
     * @return swagger 2 Docket.
     */
    @Bean
    public Docket configApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("config")
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(Predicates.not(PathSelectors.regex("/error"))) // Exclude Spring error controllers
                .build();
    }

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(Runtime.getRuntime().availableProcessors());
        factory.getContainerProperties().setPollTimeout(3000);
        return factory;
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }

    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> propsMap = new HashMap<>();
        propsMap.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBrokersCsv);
        propsMap.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        propsMap.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "100");
        propsMap.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "15000");
        propsMap.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        propsMap.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        propsMap.put(ConsumerConfig.GROUP_ID_CONFIG, "group1");
        propsMap.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return propsMap;
    }

    @Bean
    public JestClient jestClient() {
        JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(new HttpClientConfig
                .Builder(esUrl)
                .multiThreaded(true)
                .build());
        return factory.getObject();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Strawberry REST API")
                .contact("sai@concordesearch.co.uk")
                .version("1.0")
                .build();
    }


    public static void main(String[] args) {
        SpringApplicationBuilder application = new SpringApplicationBuilder();
        application //
                .headless(true) //
                .addCommandLineProperties(true) //
                .sources(EventProcessorApplication.class) //
                .main(EventProcessorApplication.class) //
                .registerShutdownHook(true)
                .run(args);
        System.out.println(" ------------- " + application);
    }


}
