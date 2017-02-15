package com.sai.strawberry.micro.actor;

import akka.actor.UntypedActor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sai.strawberry.api.EventConfig;
import com.sai.strawberry.api.NotificationConfig;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Created by saipkri on 08/09/16.
 */
public class ESPercolationSetupActor extends UntypedActor {

    private static final ObjectMapper JSONSERIALIZER = new ObjectMapper();
    private final String esUrl;

    public ESPercolationSetupActor(final String esUrl) {
        this.esUrl = esUrl;
    }


    @Override
    public void onReceive(final Object forceRecreateEsIndex) throws Throwable {
        if (forceRecreateEsIndex instanceof List) {
            Boolean force = (Boolean) ((List) forceRecreateEsIndex).get(0);
            EventConfig config = (EventConfig) ((List) forceRecreateEsIndex).get(1);
            init(force, config);
        }
    }

    // Blocking API
    public Void init(final boolean forceRecreateEsIndex, final EventConfig config) throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        System.out.println(" Force create ES index: " + forceRecreateEsIndex);
        if (forceRecreateEsIndex) {
            try {
                restTemplate.delete(esUrl + "/" + config.getConfigId());
            } catch (HttpClientErrorException ignored) {
            }
        }
        if (isIndexMissing(restTemplate, config)) {

            System.out.println("Index missing: ----- ");

            // create index.
            if (config.getDataDefinitions().getElasticsearchIndexSettings() == null) {
                restTemplate.postForObject(esUrl + "/" + config.getConfigId(), "{}", Map.class, Collections.emptyMap());
            } else {
                Map<String, Object> settings = new HashMap<>();
                settings.put("settings", config.getDataDefinitions().getElasticsearchIndexSettings());
                restTemplate.postForObject(esUrl + "/" + config.getConfigId(), JSONSERIALIZER.writeValueAsString(settings), Map.class, Collections.emptyMap());
            }

            System.out.println(" ------ " + esUrl + "/" + config.getConfigId());
            System.out.println("Now index: " + isIndexMissing(restTemplate, config));
        }

        // Apply mappings only when it is forced. Otherwise it will delete all the documents in the index.
        if (config.getDataDefinitions() != null && config.getDataDefinitions().getElasticsearchIndexDefinition() != null) {
            System.out.println(" Applying mappings now....");
            HttpEntity<String> entity = new HttpEntity<>(JSONSERIALIZER.writeValueAsString(config.getDataDefinitions().getElasticsearchIndexDefinition()));
            restTemplate.exchange(esUrl + "/" + config.getConfigId() + "/_mapping/" + config.getConfigId(), HttpMethod.PUT, entity, Map.class);
        }

        if (config.getNotification() != null
                && config.getNotification().getElasticsearch() != null
                && config.getNotification().getElasticsearch().getNotificationConfigs() != null
                && !config.getNotification().getElasticsearch().getNotificationConfigs().isEmpty()) {
            List<NotificationConfig> watchQueries = config.getNotification().getElasticsearch().getNotificationConfigs();
            Map<String, Object> percolateDoc = new LinkedHashMap<>();

            int id = 1;
            if (watchQueries != null) {
                for (NotificationConfig entry : watchQueries) {
                    percolateDoc.put("query", entry.getElasticsearchQuery());
                    percolateDoc.put("queryName", entry.getChannelName());
                    restTemplate.postForObject(esUrl + "/" + config.getConfigId() + "/.percolator/" + id, JSONSERIALIZER.writeValueAsString(percolateDoc).replace("##", "."), Object.class, Collections.emptyMap());
                    id++;
                }
            }
        }
        return null;
    }

    private boolean isIndexMissing(final RestTemplate restTemplate, final EventConfig config) {
        try {
            restTemplate.headForHeaders(esUrl + "/" + config.getConfigId());
        } catch (Exception ex) {
            return ex.getMessage().contains("404");
        }
        return false;
    }
}
