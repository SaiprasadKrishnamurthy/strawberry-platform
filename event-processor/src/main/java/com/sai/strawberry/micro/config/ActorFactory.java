package com.sai.strawberry.micro.config;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.routing.RoundRobinPool;
import com.sai.strawberry.micro.actor.*;
import io.searchbox.client.JestClient;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.springframework.data.mongodb.core.MongoTemplate;
import scala.concurrent.ExecutionContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by saipkri on 08/07/16.
 */
public class ActorFactory {

    private Map<String, ActorRef> actors = new HashMap<>();

    private final ActorSystem actorSystem;


    public ActorFactory(final ActorSystem actorSystem, final KafkaProducer<String, String> kafkaTemplate, final JestClient esFacade, final MongoTemplate mongoTemplate, final MongoTemplate batchMongoTemplate, final int esIndexBatchSize, final String esUrl, final String opsIndexName) {
        this.actorSystem = actorSystem;
        // Create the actor pool.
        actors.put(NotificationActor.class.getName(), actorSystem.actorOf(Props.create(NotificationActor.class, kafkaTemplate).withRouter(new RoundRobinPool(Runtime.getRuntime().availableProcessors()))));
        actors.put(ESIndexActor.class.getName(), actorSystem.actorOf(Props.create(ESIndexActor.class, esFacade, esIndexBatchSize).withRouter(new RoundRobinPool(Runtime.getRuntime().availableProcessors()))));
        actors.put(MongoBatchsetupActor.class.getName(), actorSystem.actorOf(Props.create(MongoBatchsetupActor.class, batchMongoTemplate).withRouter(new RoundRobinPool(Runtime.getRuntime().availableProcessors()))));
        actors.put(MongoPersistenceActor.class.getName(), actorSystem.actorOf(Props.create(MongoPersistenceActor.class, mongoTemplate).withRouter(new RoundRobinPool(Runtime.getRuntime().availableProcessors()))));
        actors.put(ESPercolationActor.class.getName(), actorSystem.actorOf(Props.create(ESPercolationActor.class, esUrl, this).withRouter(new RoundRobinPool(Runtime.getRuntime().availableProcessors()))));
        actors.put(GroovyCallbackActor.class.getName(), actorSystem.actorOf(Props.create(GroovyCallbackActor.class, mongoTemplate, batchMongoTemplate).withRouter(new RoundRobinPool(Runtime.getRuntime().availableProcessors()))));
        actors.put(KibanaActor.class.getName(), actorSystem.actorOf(Props.create(KibanaActor.class, esUrl).withRouter(new RoundRobinPool(Runtime.getRuntime().availableProcessors()))));
        actors.put(KafkaProducerActor.class.getName(), actorSystem.actorOf(Props.create(KafkaProducerActor.class, kafkaTemplate, this).withRouter(new RoundRobinPool(Runtime.getRuntime().availableProcessors()))));
        actors.put(RepositoryActor.class.getName(), actorSystem.actorOf(Props.create(RepositoryActor.class, mongoTemplate, this).withRouter(new RoundRobinPool(Runtime.getRuntime().availableProcessors()))));
        actors.put(AppCallbackActor.class.getName(), actorSystem.actorOf(Props.create(AppCallbackActor.class, mongoTemplate, batchMongoTemplate).withRouter(new RoundRobinPool(Runtime.getRuntime().availableProcessors()))));
        actors.put(ESPercolationSetupActor.class.getName(), actorSystem.actorOf(Props.create(ESPercolationSetupActor.class, esUrl).withRouter(new RoundRobinPool(Runtime.getRuntime().availableProcessors()))));
        actors.put(KibanaEngineDashboardSetupActor.class.getName(), actorSystem.actorOf(Props.create(KibanaEngineDashboardSetupActor.class, esUrl).withRouter(new RoundRobinPool(Runtime.getRuntime().availableProcessors()))));
        actors.put(OpsIndexActor.class.getName(), actorSystem.actorOf(Props.create(OpsIndexActor.class, esFacade, esIndexBatchSize, opsIndexName).withRouter(new RoundRobinPool(Runtime.getRuntime().availableProcessors()))));
    }

    public <T> ActorRef newActor(final Class<T> actorType) {
        return actors.get(actorType.getName());
    }

    public ExecutionContext executionContext() {
        return actorSystem.dispatcher();
    }
}
