package com.sai.strawberry.micro.actor;

import akka.actor.UntypedActor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sai.strawberry.api.Callback;
import com.sai.strawberry.api.Notification;
import com.sai.strawberry.micro.model.NotificationTuple;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/**
 * Created by saipkri on 08/09/16.
 */
public class WebhooksNotificationActor extends UntypedActor {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public static long timeout_in_seconds = 5 * 1000;
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void onReceive(final Object message) throws Throwable {
        if (message instanceof NotificationTuple) {

            // Additionally publish to any webhooks.
            List<String> webhooksUrl = webhooksUrl(((NotificationTuple) message).getNotificationChannel(), ((NotificationTuple) message).getContext().getConfig().getNotification());
            if (!webhooksUrl.isEmpty()) {
                for (String wh : webhooksUrl) {
                    String[] whTokens = wh.split("##");
                    String url = whTokens[0];
                    String channel = whTokens[1];
                    String transformerClass = whTokens[2];
                    Map<String, String> webhookPayload = new HashMap<>();
                    String output = "";
                    if (transformerClass != null && !transformerClass.equals("null")) {
                        output = invokeCallback(transformerClass, ((NotificationTuple) message).getContext().getDoc());
                    } else {
                        output = MAPPER.writeValueAsString(((NotificationTuple) message).getContext().getDoc());
                    }

                    webhookPayload.put("text", "*" + channel + "*\n" + output);
                    MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
                    map.add("payload", MAPPER.writeValueAsString(webhookPayload));
                    restTemplate.postForObject(url, map, String.class);
                }
            }
        }
    }

    private String invokeCallback(final String className, final Map jsonIn) throws Exception {
        Class<Callback> callback = (Class<Callback>) Class.forName(className);
        return callback.newInstance().call(jsonIn);
    }

    private List<String> webhooksUrl(final String notificationChannel, final Notification notification) {

        List<String> wh = new ArrayList<>();
        if (notification.getElasticsearch() != null) {
            wh.addAll(notification.getElasticsearch().getNotificationConfigs().stream()
                    .filter(n -> n.getChannelName().equals(notificationChannel))
                    .map(n -> n.getWebhookUrl() + "##" + n.getChannelName() + "##" + n.getWebHookDataTransformerClass())
                    .collect(toList()));

        }
        if (notification.getSql() != null) {
            wh.addAll(notification.getSql().getNotificationConfigs().stream()
                    .filter(n -> n.getChannelName().equals(notificationChannel))
                    .map(n -> n.getWebhookUrl() + "##" + n.getChannelName() + "##" + n.getWebHookDataTransformerClass())
                    .collect(toList()));
        }
        return wh;
    }
}
