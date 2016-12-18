package com.sai.strawberry.micro.eventlistener;

import akka.actor.ActorRef;
import com.sai.strawberry.micro.config.ActorFactory;
import com.sai.strawberry.micro.service.EventProcessingService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * Created by saipkri on 28/11/16.
 */
@Component
public class EventListener {

    private final ActorFactory actorFactory;

    @Inject
    public EventListener(final ActorFactory actorFactory) {
        this.actorFactory = actorFactory;
    }

    @KafkaListener(id = "id01", topics = "${kafkaInputTopic}", group = "${kafkaConsumerGroup}")
    public void listen(final ConsumerRecord<String, String> record) {
        // Async one way processing.
        actorFactory.newActor(EventProcessingService.class).tell(record.value(), ActorRef.noSender());
    }
}
