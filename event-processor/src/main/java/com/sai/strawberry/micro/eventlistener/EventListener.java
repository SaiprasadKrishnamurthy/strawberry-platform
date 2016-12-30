package com.sai.strawberry.micro.eventlistener;

import akka.actor.ActorRef;
import com.sai.strawberry.micro.config.ActorFactory;
import com.sai.strawberry.micro.service.EventProcessingService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * Created by saipkri on 28/11/16.
 */
@Component
public class EventListener implements MessageListener<String, String> {

    private final ActorFactory actorFactory;

    @Inject
    public EventListener(final ActorFactory actorFactory) {
        this.actorFactory = actorFactory;
    }

    @Override
    public void onMessage(final ConsumerRecord<String, String> consumerRecord) {
        // Async one way processing.
        actorFactory.newActor(EventProcessingService.class).tell(consumerRecord.value(), ActorRef.noSender());
    }
}
