package com.sai.strawberry.micro.actor;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sai.strawberry.api.Callback;
import com.sai.strawberry.micro.config.ActorFactory;
import com.sai.strawberry.micro.model.NotificationTuple;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Map;

/**
 * Created by saipkri on 08/09/16.
 */
public class NotificationActor extends UntypedActor {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
    }


    public static long timeout_in_seconds = 5 * 1000;

    private final KafkaProducer<String, String> sender;
    private final ActorFactory actorFactory;


    public NotificationActor(final KafkaProducer<String, String> sender, final ActorFactory actorFactory) {
        this.actorFactory = actorFactory;
        this.sender = sender;
    }

    @Override
    public void onReceive(final Object message) throws Throwable {
        if (message instanceof NotificationTuple) {
            NotificationTuple notificationTuple = (NotificationTuple) message;
            if (notificationTuple.getContext().shouldNotifyToKafkaTopic(notificationTuple.getNotificationChannel())) {
                sender.send(new ProducerRecord<>(notificationTuple.getNotificationChannel(), MAPPER.writeValueAsString(notificationTuple.getContext().getDoc())));
            }

            // Call the handler if any.
            if(StringUtils.isNotBlank(notificationTuple.getNotificationConfig().getNotificationHandlerClass())) {
                invokeCallback(notificationTuple.getNotificationConfig().getNotificationHandlerClass().trim(), notificationTuple.getContext().getDoc());
            }
            // Additionally publish to any webhooks.
            ActorRef webhooksActor = actorFactory.newActor(WebhooksNotificationActor.class);
            webhooksActor.tell(message, webhooksActor);
        }
    }

    private String invokeCallback(final String className, final Map jsonIn) throws Exception {
        Class<Callback> callback = (Class<Callback>) Class.forName(className);
        return callback.newInstance().call(jsonIn);
    }
}
