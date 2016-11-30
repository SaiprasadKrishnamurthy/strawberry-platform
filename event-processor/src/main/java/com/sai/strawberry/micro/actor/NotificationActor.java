package com.sai.strawberry.micro.actor;

import akka.actor.UntypedActor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sai.strawberry.micro.model.NotificationTuple;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

/**
 * Created by saipkri on 08/09/16.
 */
public class NotificationActor extends UntypedActor {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    public static long timeout_in_seconds = 5 * 1000;
    private final KafkaProducer<String, String> sender;


    public NotificationActor(final KafkaProducer<String, String> sender) {
        this.sender = sender;
    }

    @Override
    public void onReceive(final Object message) throws Throwable {
        if (message instanceof NotificationTuple) {
            System.out.println(" Sending to: " + ((NotificationTuple) message).getNotificationChannel());
            sender.send(new ProducerRecord<>(((NotificationTuple) message).getNotificationChannel(), MAPPER.writeValueAsString(((NotificationTuple) message).getContext().getDoc())));
        }
    }
}
