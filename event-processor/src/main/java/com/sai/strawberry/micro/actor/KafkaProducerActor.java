package com.sai.strawberry.micro.actor;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.dispatch.OnComplete;
import akka.pattern.Patterns;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sai.strawberry.api.EventStreamConfig;
import com.sai.strawberry.micro.config.ActorFactory;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import scala.concurrent.Future;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by saipkri on 08/09/16.
 */
public class KafkaProducerActor extends UntypedActor {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    public static long timeout_in_seconds = 5 * 1000;
    private final KafkaProducer<String, String> sender;
    private final ActorFactory actorFactory;


    public KafkaProducerActor(final KafkaProducer<String, String> sender, final ActorFactory actorFactory) {
        this.sender = sender;
        this.actorFactory = actorFactory;
    }

    @Override
    public void onReceive(final Object message) throws Throwable {
        System.out.println("\t\t ---- Kafka Producer Actor: " + message);
        System.out.println("Message instanceof map? " + (message instanceof Map));
        if (message instanceof Map) {
            ActorRef repositoryActor = actorFactory.newActor(RepositoryActor.class);
            Future<Object> eventStreamConfigFuture = Patterns.ask(repositoryActor, ((Map) message).get("topic"), RepositoryActor.timeout_in_seconds);
            eventStreamConfigFuture.onComplete(new OnComplete<Object>() {
                @Override
                public void onComplete(Throwable failure, Object success) throws Throwable {
                    EventStreamConfig eventStreamConfig = (EventStreamConfig) success;
                    if (!eventStreamConfig.isEnabled()) {
                        getSender().tell(false, getSelf());
                    } else {
                        Map<String, Object> doc = new HashMap<>();
                        doc.put("eventStreamConfig", eventStreamConfig);
                        doc.put("payload", ((Map) message).get("payload"));
                        try {
                            System.out.println("Before sending to kafka: --> " + sender + " , " + eventStreamConfig.getConfigId());
                            sender.send(new ProducerRecord<>(eventStreamConfig.getConfigId(), MAPPER.writeValueAsString(doc)));
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }, actorFactory.executionContext());
            getSender().tell(true, getSelf());
        } else {
            getSender().tell(false, getSelf());
        }
    }
}
