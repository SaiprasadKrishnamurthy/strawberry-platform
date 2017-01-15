package com.sai.strawberry.micro;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.pattern.Patterns;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.HostDistance;
import com.datastax.driver.core.PoolingOptions;
import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.MappingManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Predicates;
import com.mongodb.MongoClient;
import com.sai.strawberry.api.EventConfig;
import com.sai.strawberry.micro.actor.*;
import com.sai.strawberry.micro.config.ActorFactory;
import com.sai.strawberry.micro.eventlistener.EventListener;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.io.IOUtils;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.config.ContainerProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import scala.concurrent.Future;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.inject.Inject;
import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Created by saipkri on 07/09/16.
 */
@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = {"com.sai.strawberry.micro", "${springBeansPkgs:''}"})
@EnableMongoRepositories
//@EnableEurekaClient
@EnableSwagger2
@EnableKafka
@EnableAsync
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

    @Value("${cleanupExistingIndex}")
    private boolean cleanupExistingIndex;

    @Value("${kibanaOpsIndexName ?: strawberryOpsIdx}")
    private String kibanaOpsIndexName;

    @Value("${sqlDbConnectionPoolSize ?: 30}")
    private int sqlDbConnectionPoolSize;

    @Value("${cassandraSeedNodes ?: 127.0.0.1}")
    private String cassandraSeedNodes;

    @Value("${cassandraConnectionPoolSize ?: 10}")
    private int cassandraConnectionPoolSize;

    @Value("${kafkaConsumerGroup}")
    private String kafkaConsumerGroup;

    private Session cassandraSession;

    private MappingManager cassandraMappingManager;

    private Set<String> configIds = new HashSet<>();

    private ActorSystem actorSystem() {
        return ActorSystem.create("StrawberryActorSystem");
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer properties() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public ActorFactory actorFactory(final MongoTemplate mongoTemplate) throws Exception {
        ActorFactory actorFactory = new ActorFactory(actorSystem(), kafkaProducer(), jestClient(), mongoTemplate, mongoForBatch(), esIndexBatchSize, esUrl, kibanaOpsIndexName.toLowerCase().trim(), jdbcTemplate(), cassandraCluster(), cassandraSession, cassandraMappingManager);
        initConfigs(actorFactory);
        return actorFactory;
    }

    @Bean
    public MongoTemplate mongoTemplate(final MongoDbFactory mongoDbFactory, final MappingMongoConverter mappingMongoConverter) {
        MongoTemplate mongoTemplate = new MongoTemplate(mongoDbFactory, mappingMongoConverter);
        return mongoTemplate;
    }

    @Bean(destroyMethod = "close")
    public Cluster cassandraCluster() {
        try {
            PoolingOptions poolingOptions = new PoolingOptions();
            poolingOptions.setMaxRequestsPerConnection(HostDistance.LOCAL, cassandraConnectionPoolSize);
            Cluster.Builder builder = Cluster.builder();
            Cluster cluster = builder.addContactPoints(Stream.of(cassandraSeedNodes)
                    .map(ip -> ip.trim())
                    .collect(toList())
                    .toArray(new String[cassandraSeedNodes.split(",").length]))
                    .withPoolingOptions(poolingOptions)
                    .build();
            cassandraSession = cluster.connect();
            cassandraMappingManager = new MappingManager(cassandraSession);
            return cluster;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Bean
    public EventListener eventListener(final ActorFactory actorFactory) {
        return new EventListener(actorFactory);
    }

    private void initConfigs(final ActorFactory actorFactory) {
        final ObjectMapper jsonParser = new ObjectMapper();
        System.out.println("\n\n\n------------- INITIALIZING CONFIGSS --------------------");
        Stream.of(configs)
                .map(resource -> {
                    try {
                        System.out.println(" ******** " + resource.getFilename());
                        return Optional.of(jsonParser.readValue(IOUtils.toString(resource.getInputStream()), EventConfig.class));
                    } catch (IOException e) {
                        LOGGER.info("Didn't load " + resource + " in config db. Doesn't appear to be a config file.");
                        return Optional.empty();
                    }
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(config -> {
                    EventConfig eventConfig = (EventConfig) config;

                    configIds.add(eventConfig.getConfigId().trim());

                    ActorRef watcherSqlDbSetupActor = actorFactory.newActor(WatcherSQLDBSetupActor.class);
                    Future<Object> watcherSqlDbSetupActorFuture = Patterns.ask(watcherSqlDbSetupActor, config, WatcherSQLDBSetupActor.timeout_in_seconds);
                    ActorRef repoActor = actorFactory.newActor(RepositoryActor.class);

                    Patterns.pipe(watcherSqlDbSetupActorFuture, actorFactory.executionContext())
                            .to(repoActor);

                    // Setup the percolation.
                    ActorRef esPercolationSetupActor = actorFactory.newActor(ESPercolationSetupActor.class);
                    esPercolationSetupActor.tell(Arrays.asList(cleanupExistingIndex, config), ActorRef.noSender());

                    // Setup the ops dashboard.
                    if (eventConfig.isEnableVisualization()) {
                        ActorRef opsDashboard = actorFactory.newActor(KibanaEngineDashboardSetupActor.class);
                        opsDashboard.tell(kibanaOpsIndexName, ActorRef.noSender());
                    }

                    // Cassandra setup.
                    if (eventConfig.getDataDefinitions() != null
                            && eventConfig.getDataDefinitions().getDatabase() != null
                            && eventConfig.getDataDefinitions().getDatabase().getCassandra() != null) {
                        ActorRef cassandraSetup = actorFactory.newActor(CassandraDDLSetupActor.class);
                        cassandraSetup.tell(config, ActorRef.noSender());
                    }
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

    @Bean
    public JdbcTemplate jdbcTemplate() {
        BasicDataSource ds = new BasicDataSource();
        ds.setUrl("jdbc:h2:mem:strawberry-memdb;DB_CLOSE_DELAY=-1");
        ds.setDriverClassName("org.h2.Driver");
        ds.setInitialSize(sqlDbConnectionPoolSize);
        ds.setMaxTotal(sqlDbConnectionPoolSize);
        ds.setPoolPreparedStatements(true);
        ds.setMaxOpenPreparedStatements(sqlDbConnectionPoolSize);
        return new JdbcTemplate(ds);
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
    public ConcurrentMessageListenerContainer<String, String> kafkaListenerContainer(final EventListener eventListener) {
        System.out.println(" Config ids: " + configIds);
        ContainerProperties props = new ContainerProperties(configIds.toArray(new String[configIds.size()]));
        props.setMessageListener(eventListener);
        ConcurrentMessageListenerContainer<String, String> container = new ConcurrentMessageListenerContainer<>(consumerFactory(), props);
        container.setConcurrency(Runtime.getRuntime().availableProcessors());
        return container;
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
        propsMap.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "200");
        propsMap.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "12000");
        propsMap.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        propsMap.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        propsMap.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaConsumerGroup);
        propsMap.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        propsMap.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, 13000);
        propsMap.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 3000);
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
