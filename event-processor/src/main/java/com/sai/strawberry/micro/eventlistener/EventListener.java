package com.sai.strawberry.micro.eventlistener;

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

    private final EventProcessingService eventProcessingService;

    @Inject
    public EventListener(final EventProcessingService eventProcessingService) {
        this.eventProcessingService = eventProcessingService;
    }

    @KafkaListener(id = "id01", topics = "${kafkaInputTopic}", group = "${kafkaConsumerGroup}")
    public void listen(final ConsumerRecord<String, String> record) {
        eventProcessingService.process(record.value());
    }
}
