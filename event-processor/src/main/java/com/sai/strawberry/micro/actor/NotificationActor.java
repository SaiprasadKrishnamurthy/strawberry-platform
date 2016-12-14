package com.sai.strawberry.micro.actor;

import akka.actor.UntypedActor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sai.strawberry.micro.model.NotificationTuple;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
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
    private final RestTemplate restTemplate = new RestTemplate();


    public NotificationActor(final KafkaProducer<String, String> sender) {
        this.sender = sender;
    }

    @Override
    public void onReceive(final Object message) throws Throwable {
        if (message instanceof NotificationTuple) {
            sender.send(new ProducerRecord<>(((NotificationTuple) message).getNotificationChannel(), MAPPER.writeValueAsString(((NotificationTuple) message).getContext().getDoc())));

            // Additionally publish to any webhooks.
            if (StringUtils.isNotBlank(((NotificationTuple) message).getContext().getConfig().getNotification().getWebhooksUrl())) {
                String webhooks = ((NotificationTuple) message).getContext().getConfig().getNotification().getWebhooksUrl();
                String channelName = ((NotificationTuple) message).getNotificationChannel();
                String payload = MAPPER.writeValueAsString(((NotificationTuple) message).getContext().getDoc());
                Map<String, String> webhookPayload = new HashMap<>();
                webhookPayload.put("text", "*"+channelName+"*\n"+payload);
                MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
                map.add("payload", MAPPER.writeValueAsString(webhookPayload));
                String result = restTemplate.postForObject(webhooks, map, String.class);
            }
        }
    }
}
