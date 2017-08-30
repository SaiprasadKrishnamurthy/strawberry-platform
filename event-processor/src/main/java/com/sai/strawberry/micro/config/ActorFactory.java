package com.sai.strawberry.micro.config;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.routing.RoundRobinPool;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.MappingManager;
import com.sai.strawberry.micro.actor.*;
import com.sai.strawberry.micro.service.EventProcessingService;
import io.searchbox.client.JestClient;
import org.apache.activemq.spring.ActiveMQConnectionFactory;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.neo4j.ogm.session.SessionFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import scala.concurrent.ExecutionContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by saipkri on 08/07/16.
 */
public class ActorFactory {

    private Map<String, ActorRef> actors = new HashMap<>();

    private final ActorSystem actorSystem;


    public ActorFactory(final ActorSystem actorSystem, final KafkaProducer<String, String> kafkaTemplate, final JestClient esFacade, final MongoTemplate mongoTemplate, final MongoTemplate batchMongoTemplate, final int esIndexBatchSize, final String esUrl, final String opsIndexName, final JdbcTemplate jdbcTemplate, final Cluster cassandraCluster, final Session cassandraSession, final MappingManager cassandraMappingManager, SessionFactory sessionFactory, final ActiveMQConnectionFactory activeMQConnectionFactory) {
        this.actorSystem = actorSystem;
        // Create the actor pool.
        actors.put(NotificationActor.class.getName(), actorSystem.actorOf(Props.create(NotificationActor.class, kafkaTemplate, this, mongoTemplate, batchMongoTemplate, cassandraSession, cassandraMappingManager).withRouter(new RoundRobinPool(Runtime.getRuntime().availableProcessors()))));
        actors.put(ESIndexActor.class.getName(), actorSystem.actorOf(Props.create(ESIndexActor.class, esFacade, esIndexBatchSize).withRouter(new RoundRobinPool(Runtime.getRuntime().availableProcessors()))));
        actors.put(MongoBatchsetupActor.class.getName(), actorSystem.actorOf(Props.create(MongoBatchsetupActor.class, mongoTemplate, batchMongoTemplate).withRouter(new RoundRobinPool(Runtime.getRuntime().availableProcessors()))));
        actors.put(MongoPersistenceActor.class.getName(), actorSystem.actorOf(Props.create(MongoPersistenceActor.class, mongoTemplate, batchMongoTemplate).withRouter(new RoundRobinPool(Runtime.getRuntime().availableProcessors()))));
        actors.put(ESPercolationActor.class.getName(), actorSystem.actorOf(Props.create(ESPercolationActor.class, esUrl, this).withRouter(new RoundRobinPool(Runtime.getRuntime().availableProcessors()))));
        actors.put(KibanaActor.class.getName(), actorSystem.actorOf(Props.create(KibanaActor.class, esUrl).withRouter(new RoundRobinPool(Runtime.getRuntime().availableProcessors()))));
        actors.put(KafkaProducerActor.class.getName(), actorSystem.actorOf(Props.create(KafkaProducerActor.class, kafkaTemplate, this).withRouter(new RoundRobinPool(Runtime.getRuntime().availableProcessors()))));
        actors.put(RepositoryActor.class.getName(), actorSystem.actorOf(Props.create(RepositoryActor.class, mongoTemplate, this).withRouter(new RoundRobinPool(Runtime.getRuntime().availableProcessors()))));
        actors.put(AppCallbackActor.class.getName(), actorSystem.actorOf(Props.create(AppCallbackActor.class, mongoTemplate, batchMongoTemplate, cassandraSession, sessionFactory, this).withRouter(new RoundRobinPool(Runtime.getRuntime().availableProcessors()))));
        actors.put(ESPercolationSetupActor.class.getName(), actorSystem.actorOf(Props.create(ESPercolationSetupActor.class, esUrl).withRouter(new RoundRobinPool(Runtime.getRuntime().availableProcessors()))));
        actors.put(KibanaEngineDashboardSetupActor.class.getName(), actorSystem.actorOf(Props.create(KibanaEngineDashboardSetupActor.class, esUrl).withRouter(new RoundRobinPool(Runtime.getRuntime().availableProcessors()))));
        actors.put(OpsIndexActor.class.getName(), actorSystem.actorOf(Props.create(OpsIndexActor.class, esFacade, esIndexBatchSize, opsIndexName).withRouter(new RoundRobinPool(Runtime.getRuntime().availableProcessors()))));
        actors.put(WatcherSQLDBActor.class.getName(), actorSystem.actorOf(Props.create(WatcherSQLDBActor.class, jdbcTemplate, this).withRouter(new RoundRobinPool(Runtime.getRuntime().availableProcessors()))));
        actors.put(WatcherSQLDBSetupActor.class.getName(), actorSystem.actorOf(Props.create(WatcherSQLDBSetupActor.class, jdbcTemplate).withRouter(new RoundRobinPool(Runtime.getRuntime().availableProcessors()))));
        actors.put(WatcherSQLDBCleanupActor.class.getName(), actorSystem.actorOf(Props.create(WatcherSQLDBCleanupActor.class, jdbcTemplate).withRouter(new RoundRobinPool(Runtime.getRuntime().availableProcessors()))));
        actors.put(EventProcessingService.class.getName(), actorSystem.actorOf(Props.create(EventProcessingService.class, mongoTemplate, batchMongoTemplate, cassandraSession, cassandraMappingManager, this).withRouter(new RoundRobinPool(Runtime.getRuntime().availableProcessors()))));
        actors.put(WebhooksNotificationActor.class.getName(), actorSystem.actorOf(Props.create(WebhooksNotificationActor.class, mongoTemplate, batchMongoTemplate, cassandraSession, cassandraMappingManager).withRouter(new RoundRobinPool(Runtime.getRuntime().availableProcessors()))));
        actors.put(CassandraDDLSetupActor.class.getName(), actorSystem.actorOf(Props.create(CassandraDDLSetupActor.class, cassandraCluster).withRouter(new RoundRobinPool(Runtime.getRuntime().availableProcessors()))));
        actors.put(SpelExpressionEvaluationActor.class.getName(), actorSystem.actorOf(Props.create(SpelExpressionEvaluationActor.class, this).withRouter(new RoundRobinPool(Runtime.getRuntime().availableProcessors()))));
        actors.put(ESSearchActor.class.getName(), actorSystem.actorOf(Props.create(ESSearchActor.class, esUrl).withRouter(new RoundRobinPool(Runtime.getRuntime().availableProcessors()))));
        actors.put(PreNotificationChecksActor.class.getName(), actorSystem.actorOf(Props.create(PreNotificationChecksActor.class, this, mongoTemplate, batchMongoTemplate, cassandraSession, cassandraMappingManager).withRouter(new RoundRobinPool(Runtime.getRuntime().availableProcessors()))));
        actors.put(AmqProducerActor.class.getName(), actorSystem.actorOf(Props.create(AmqProducerActor.class, activeMQConnectionFactory, this).withRouter(new RoundRobinPool(Runtime.getRuntime().availableProcessors()))));
    }

    public <T> ActorRef newActor(final Class<T> actorType) {
        return actors.get(actorType.getName());
    }

    public ExecutionContext executionContext() {
        return actorSystem.dispatcher();
    }
}
