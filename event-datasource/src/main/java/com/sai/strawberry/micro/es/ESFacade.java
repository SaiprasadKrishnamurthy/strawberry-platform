package com.sai.strawberry.micro.es;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sai.strawberry.api.EventStreamConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Sai
 */
public class ESFacade {
    private final String esUrl;
    private final static ObjectMapper JSONSERIALIZER = new ObjectMapper();
    private static final Logger LOG = LoggerFactory.getLogger(ESFacade.class);

    @Inject
    public ESFacade(final String esUrl) throws Exception {
        this.esUrl = esUrl;
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
                LOG.info("\n\n");

                LOG.info("Creating es index: " + config.getConfigId());
                // create index.
                restTemplate.postForObject(esUrl + "/" + config.getConfigId(), "{}", Map.class, Collections.emptyMap());

                LOG.info("Creating es mapping for the type: " + config.getConfigId());
                // apply mappings.
                if (config.getIndexDefinition() != null && !config.getIndexDefinition().isEmpty()) {
                    restTemplate.postForObject(esUrl + "/" + config.getConfigId() + "/_mapping/" + config.getConfigId(), JSONSERIALIZER.writeValueAsString(config.getIndexDefinition()), Map.class, Collections.emptyMap());
                }
                LOG.info("\n\n");

                Map<String, Map<String, Object>> watchQueries = config.getWatchQueries();
                Map<String, Object> percolateDoc = new LinkedHashMap<>();

                int id = 1;
                for (Map.Entry<String, Map<String, Object>> entry : watchQueries.entrySet()) {
                    percolateDoc.put("query", entry.getValue());
                    percolateDoc.put("queryName", entry.getKey());
                    restTemplate.postForObject(esUrl + "/" + config.getConfigId() + "/.percolator/" + id, JSONSERIALIZER.writeValueAsString(percolateDoc).replace("##", "."), Object.class, Collections.emptyMap());
                    id++;
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
