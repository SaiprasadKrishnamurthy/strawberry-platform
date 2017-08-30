package com.sai.strawberry.micro.actor;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.dispatch.OnComplete;
import akka.pattern.Patterns;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sai.strawberry.api.EventConfig;
import com.sai.strawberry.micro.config.ActorFactory;
import org.apache.activemq.spring.ActiveMQConnectionFactory;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import scala.concurrent.Future;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by saipkri on 08/09/16.
 */
public class AmqProducerActor extends UntypedActor {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    public static long timeout_in_seconds = 5 * 1000;
    private final ActiveMQConnectionFactory activeMQConnectionFactory;
    private final ActorFactory actorFactory;
    private final JmsTemplate jmsTemplate;


    public AmqProducerActor(ActiveMQConnectionFactory activeMQConnectionFactory, final ActorFactory actorFactory) {
        this.activeMQConnectionFactory = activeMQConnectionFactory;
        this.actorFactory = actorFactory;
        this.jmsTemplate = new JmsTemplate(activeMQConnectionFactory);
    }

    @Override
    public void onReceive(final Object message) throws Throwable {
        if (message instanceof Map) {
            ActorRef repositoryActor = actorFactory.newActor(RepositoryActor.class);
            Future<Object> eventStreamConfigFuture = Patterns.ask(repositoryActor, ((Map) message).get("topic"), RepositoryActor.timeout_in_seconds);
            eventStreamConfigFuture.onComplete(new OnComplete<Object>() {
                @Override
                public void onComplete(Throwable failure, Object success) throws Throwable {
                    EventConfig eventStreamConfig = (EventConfig) success;
                    if (!eventStreamConfig.isEnabled()) {
                        getSender().tell(false, getSelf());
                    } else {
                        Map<String, Object> doc = new HashMap<>();
                        doc.put("eventStreamConfig", eventStreamConfig);
                        doc.put("payload", ((Map) message).get("payload"));
                        doc.put("timestamp", System.currentTimeMillis());
                        try {
                            jmsTemplate.send(eventStreamConfig.getConfigId(), (MessageCreator) session -> {
                                try {
                                    TextMessage msg = session.createTextMessage();
                                    msg.setText(MAPPER.writeValueAsString(doc));
                                    return msg;
                                } catch (JsonProcessingException e) {
                                    e.printStackTrace();
                                    throw new RuntimeException(e);
                                }
                            });
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
