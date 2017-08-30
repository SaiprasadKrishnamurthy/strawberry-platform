package com.sai.strawberry.micro.eventlistener;

import akka.actor.ActorRef;
import com.sai.strawberry.micro.config.ActorFactory;
import com.sai.strawberry.micro.service.EventProcessingService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

/**
 * Created by saipkri on 28/11/16.
 */
public class JmsEventListener implements javax.jms.MessageListener {

    private final ActorFactory actorFactory;

    public JmsEventListener(final ActorFactory actorFactory) {
        this.actorFactory = actorFactory;
    }


    @Override
    public void onMessage(Message message) {
        TextMessage msg = (TextMessage)message;
        try {
            actorFactory.newActor(EventProcessingService.class).tell(msg.getText(), ActorRef.noSender());
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
