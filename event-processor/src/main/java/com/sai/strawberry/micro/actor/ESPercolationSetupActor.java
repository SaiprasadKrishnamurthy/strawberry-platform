package com.sai.strawberry.micro.actor;

import akka.actor.UntypedActor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sai.strawberry.api.EventStreamConfig;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
            EventStreamConfig config = (EventStreamConfig) ((List) forceRecreateEsIndex).get(1);
            init(force, config);
        }
    }

    // Blocking API
    public Void init(final boolean forceRecreateEsIndex, final EventStreamConfig config) throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        try {
            if (forceRecreateEsIndex) {
                try {
                    restTemplate.delete(esUrl + "/" + config.getConfigId());
                } catch (HttpClientErrorException ignored) {
                }
            }
            if (isIndexMissing(restTemplate, config)) {

                // create index.
                restTemplate.postForObject(esUrl + "/" + config.getConfigId(), "{}", Map.class, Collections.emptyMap());

                // apply mappings.
                if (config.getIndexDefinition() != null && !config.getIndexDefinition().isEmpty()) {
                    restTemplate.postForObject(esUrl + "/" + config.getConfigId() + "/_mapping/" + config.getConfigId(), JSONSERIALIZER.writeValueAsString(config.getIndexDefinition()), Map.class, Collections.emptyMap());
                }

                Map<String, Map<String, Object>> watchQueries = config.getWatchQueries();
                Map<String, Object> percolateDoc = new LinkedHashMap<>();

                int id = 1;
                if (watchQueries != null) {
                    for (Map.Entry<String, Map<String, Object>> entry : watchQueries.entrySet()) {
                        percolateDoc.put("query", entry.getValue());
                        percolateDoc.put("queryName", entry.getKey());
                        restTemplate.postForObject(esUrl + "/" + config.getConfigId() + "/.percolator/" + id, JSONSERIALIZER.writeValueAsString(percolateDoc).replace("##", "."), Object.class, Collections.emptyMap());
                        id++;
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        return null;
    }

    private boolean isIndexMissing(final RestTemplate restTemplate, final EventStreamConfig config) {
        try {
            restTemplate.headForHeaders(esUrl + "/" + config.getConfigId());
        } catch (Exception ex) {
            return ex.getMessage().contains("404");
        }
        return false;
    }
}
