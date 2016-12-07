package com.sai.strawberry.micro.actor;

import akka.actor.UntypedActor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sai.strawberry.micro.model.EventProcessingContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by saipkri on 08/09/16.
 */
public class KibanaActor extends UntypedActor {

    private final String esUrl;
    private ObjectMapper objectMapper = new ObjectMapper();


    public KibanaActor(final String esUrl) {
        this.esUrl = esUrl;
    }

    @Override
    public void onReceive(final Object message) throws Throwable {
        if (message instanceof EventProcessingContext) {
            EventProcessingContext context = (EventProcessingContext) message;
            if (context.getConfig().isEnableVisualization()) {
                // Find if the index pattern is already defined in Kibana.
                RestTemplate rt = new RestTemplate();
                boolean found = true;
                String indexCheckUrl = esUrl + "/.kibana/index-pattern/" + context.getConfig().getConfigId();
                try {
                    rt.getForObject(indexCheckUrl, Map.class, Collections.emptyMap());
                } catch (Exception ex) {
                    found = false;
                }
                if (!found) {
                    String endpoint = ".kibana/index-pattern/" + context.getConfig().getConfigId();
                    Map<String, String> in = new LinkedHashMap<>();
                    in.put("title", context.getConfig().getConfigId());
                    Map<String, Map> props = (Map<String, Map>) context.getConfig().getIndexDefinition().get("properties");
                    Optional<Map.Entry<String, Map>> timestampField = props.entrySet().stream().filter(entry -> entry.getValue().containsValue("date")).findFirst();
                    if (timestampField.isPresent()) {
                        in.put("timeFieldName", timestampField.get().getKey().trim());
                    }
                    objectMapper = new ObjectMapper();
                    rt.exchange(esUrl + "/" + endpoint, HttpMethod.PUT, new HttpEntity<Object>(objectMapper.writeValueAsString(in)), Map.class, Collections.emptyMap());
                    endpoint = ".kibana/config/4.6.1";
                    in.clear();
                    in.put("defaultIndex", context.getConfig().getConfigId());
                    rt.exchange(esUrl + "/" + endpoint, HttpMethod.PUT, new HttpEntity<Object>(objectMapper.writeValueAsString(in)), Map.class, Collections.emptyMap());
                }
            }
        }
    }
}
