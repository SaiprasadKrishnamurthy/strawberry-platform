package com.sai.strawberry.micro.config;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.routing.RoundRobinPool;
import com.sai.strawberry.micro.actor.ESActor;
import com.sai.strawberry.micro.actor.KafkaProducerActor;
import com.sai.strawberry.micro.actor.RepositoryActor;
import com.sai.strawberry.micro.es.ESFacade;
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
    private final ESFacade esFacade;


    public ActorFactory(final ActorSystem actorSystem, final KafkaProducer<String, String> kafkaTemplate, final ESFacade esFacade, final MongoTemplate mongoTemplate) {
        this.actorSystem = actorSystem;
        this.esFacade = esFacade;
        // Create the actor pool.
        actors.put(KafkaProducerActor.class.getName(), actorSystem.actorOf(Props.create(KafkaProducerActor.class, kafkaTemplate, this).withRouter(new RoundRobinPool(Runtime.getRuntime().availableProcessors()))));
        actors.put(RepositoryActor.class.getName(), actorSystem.actorOf(Props.create(RepositoryActor.class, mongoTemplate, this).withRouter(new RoundRobinPool(Runtime.getRuntime().availableProcessors()))));
        actors.put(ESActor.class.getName(), actorSystem.actorOf(Props.create(ESActor.class, esFacade).withRouter(new RoundRobinPool(Runtime.getRuntime().availableProcessors()))));
    }

    public <T> ActorRef newActor(final Class<T> actorType) {
        return actors.get(actorType.getName());
    }

    public ExecutionContext executionContext() {
        return actorSystem.dispatcher();
    }
}
