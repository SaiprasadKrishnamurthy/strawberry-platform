package com.sai.strawberry.micro.actor;

import akka.actor.UntypedActor;
import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.MappingManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sai.strawberry.api.*;
import com.sai.strawberry.micro.model.NotificationTuple;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * Created by saipkri on 08/09/16.
 */
public class WebhooksNotificationActor extends UntypedActor {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static long timeout_in_seconds = 5 * 1000;
    private final RestTemplate restTemplate = new RestTemplate();
    private final MongoTemplate mongoTemplate;
    private final MongoTemplate mongoBatchDBTemplate;
    private final Session cassandraSession;
    private final MappingManager cassandraMappingManager;

    public WebhooksNotificationActor(final MongoTemplate mongoTemplate, final MongoTemplate mongoBatchDBTemplate, final Session cassandraSession, final MappingManager cassandraMappingManager) {
        this.mongoTemplate = mongoTemplate;
        this.mongoBatchDBTemplate = mongoBatchDBTemplate;
        this.cassandraMappingManager = cassandraMappingManager;
        this.cassandraSession = cassandraSession;
    }

    @Override
    public void onReceive(final Object message) throws Throwable {
        if (message instanceof NotificationTuple) {

            // Additionally publish to any webhooks.
            NotificationTuple notifTuple = (NotificationTuple) message;
            List<String> webhooksUrl = webhooksUrl(notifTuple.getNotificationChannel(),
                    notifTuple.getContext().getConfig().getNotification(),
                    notifTuple.getContext().getConfig(),
                    notifTuple.getContext().getDoc());

            if (!webhooksUrl.isEmpty()) {
                for (String wh : webhooksUrl) {
                    String[] whTokens = wh.split("##");
                    String url = whTokens[0];
                    if (url != null && !url.equals("null")) {
                        String transformerClass = whTokens[2];
                        Map<String, String> webhookPayload = new HashMap<>();
                        String output;
                        if (transformerClass != null && !transformerClass.equals("null")) {
                            output = invokeCallback(transformerClass, notifTuple.getContext().getDoc());
                        } else {
                            output = MAPPER.writeValueAsString(notifTuple.getContext().getDoc());
                        }

                        webhookPayload.put("text", "* Notification for event: " + notifTuple.getContext().getConfig().getConfigId() + "*\n" + output);
                        webhookPayload.put("icon_emoji", ":strawberry:");
                        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
                        map.add("payload", MAPPER.writeValueAsString(webhookPayload));
                        restTemplate.postForObject(url, map, String.class);
                    }
                }
            }
        }
    }

    private String invokeCallback(final String className, final Map jsonIn) throws Exception {
        Class<Callback> callback = (Class<Callback>) Class.forName(className);
        return callback.newInstance().call(jsonIn);
    }

    private List<String> webhooksUrl(final String notificationChannel, final Notification notification, final EventConfig eventConfig, final Map doc) {

        List<String> wh = new ArrayList<>();
        if (notification.getElasticsearch() != null) {
            List<String> urls = notification.getElasticsearch().getNotificationConfigs().stream()
                    .filter(n -> n.getChannelName().equals(notificationChannel))
                    .map(n -> n.getWebhookUrl() + "##" + n.getChannelName() + "##" + n.getWebHookDataTransformerClass())
                    .collect(toList());
            wh.addAll(urls);

            collectWebhookUrlsFromProviders(notification.getElasticsearch().getNotificationConfigs(), notificationChannel, notification, eventConfig, doc, wh);
        }
        if (notification.getSql() != null) {
            wh.addAll(notification.getSql().getNotificationConfigs().stream()
                    .filter(n -> n.getChannelName().equals(notificationChannel))
                    .map(n -> n.getWebhookUrl() + "##" + n.getChannelName() + "##" + n.getWebHookDataTransformerClass())
                    .collect(toList()));
            collectWebhookUrlsFromProviders(notification.getSql().getNotificationConfigs(), notificationChannel, notification, eventConfig, doc, wh);

        }
        if (notification.getSpel() != null) {
            wh.addAll(notification.getSpel().getNotificationConfigs().stream()
                    .filter(n -> n.getChannelName().equals(notificationChannel))
                    .map(n -> n.getWebhookUrl() + "##" + n.getChannelName() + "##" + n.getWebHookDataTransformerClass())
                    .collect(toList()));
            collectWebhookUrlsFromProviders(notification.getSpel().getNotificationConfigs(), notificationChannel, notification, eventConfig, doc, wh);
        }
        return wh;
    }

    private void collectWebhookUrlsFromProviders(final List<NotificationConfig> notificationConfigs, final String notificationChannel, final Notification notification, final EventConfig eventConfig, final Map doc, final List<String> wh) {
        for (NotificationConfig notificationConfig : notificationConfigs) {
            if (notificationConfig.getChannelName().equals(notificationChannel)) {
                List<String> webhookUrlsFromProviders = webHookUrlsFromProviders(eventConfig, doc, notificationConfig.getWebhookUrlProviderClass());
                wh.addAll(webhookUrlsFromProviders.stream()
                        .map(url -> url + "##" + notificationConfig.getChannelName() + "##" + notificationConfig.getWebHookDataTransformerClass())
                        .collect(Collectors.toList()));
            }
        }
    }

    private List<String> webHookUrlsFromProviders(final EventConfig config, final Map doc, final String providerClass) {
        if (StringUtils.isNotBlank(providerClass)) {
            try {
                WebHooksUrlProvider webHooksUrlProvider = (WebHooksUrlProvider) Class.forName(providerClass.trim()).newInstance();
                return webHooksUrlProvider.webHookUrl(new ConditionEvaluatorParamsHolder(mongoTemplate, mongoBatchDBTemplate, cassandraSession, cassandraMappingManager, config, doc));
            } catch (Exception ex) {
                ex.printStackTrace();
                return Collections.emptyList();
            }
        }
        return Collections.emptyList();
    }
}
