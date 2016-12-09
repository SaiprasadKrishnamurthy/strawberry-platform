package com.sai.strawberry.micro.actor;

import akka.actor.UntypedActor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Created by saipkri on 08/09/16.
 */
public class KibanaEngineDashboardSetupActor extends UntypedActor {

    private final String esUrl;
    private ObjectMapper objectMapper = new ObjectMapper();

    public KibanaEngineDashboardSetupActor(final String esUrl) {
        this.esUrl = esUrl;
    }

    @Override
    public void onReceive(final Object message) throws Throwable {
        if (message instanceof String) {
            String opsIndexName = ((String) message).toLowerCase().trim();

            // Find if the index pattern is already defined in Kibana.
            RestTemplate rt = new RestTemplate();

            if (isIndexMissing(rt, opsIndexName)) {

                // create index.
                rt.postForObject(esUrl + "/" + opsIndexName, "{}", Map.class, Collections.emptyMap());

                // apply mappings.
                rt.postForObject(esUrl + "/" + opsIndexName + "/_mapping/" + opsIndexName, IOUtils.toString(KibanaEngineDashboardSetupActor.class.getClassLoader().getResourceAsStream("kibanaOpsIndex.json")), Map.class, Collections.emptyMap());

                String indexCheckUrl = esUrl + "/.kibana/index-pattern/" + opsIndexName;
                boolean found = true;
                try {
                    rt.getForObject(indexCheckUrl, Map.class, Collections.emptyMap());
                } catch (Exception ex) {
                    found = false;
                }
                if (!found) {
                    String endpoint = ".kibana/index-pattern/" + opsIndexName;
                    Map<String, String> in = new LinkedHashMap<>();
                    in.put("title", opsIndexName);
                    Map<String, Map> props = (Map<String, Map>) objectMapper.readValue(IOUtils.toString(KibanaEngineDashboardSetupActor.class.getClassLoader().getResourceAsStream("kibanaOpsIndex.json")), Map.class);
                    Optional<Map.Entry<String, Map>> timestampField = props.entrySet().stream().filter(entry -> entry.getValue().containsValue("date")).findFirst();
                    if (timestampField.isPresent()) {
                        in.put("timeFieldName", timestampField.get().getKey().trim());
                    }
                    rt.exchange(esUrl + "/" + endpoint, HttpMethod.PUT, new HttpEntity<Object>(objectMapper.writeValueAsString(in)), Map.class, Collections.emptyMap());
                    endpoint = ".kibana/config/4.6.1";
                    in.clear();
                    in.put("defaultIndex", opsIndexName);
                    rt.exchange(esUrl + "/" + endpoint, HttpMethod.PUT, new HttpEntity<Object>(objectMapper.writeValueAsString(in)), Map.class, Collections.emptyMap());
                }
            }

            // Load canned objects into Kibana via ES.
            List<Map<String, Object>> cannedObjects = objectMapper.readValue(IOUtils.toString(KibanaEngineDashboardSetupActor.class.getClassLoader().getResourceAsStream("opsDashboard.json")), List.class);

            cannedObjects.forEach(object -> {
                String id = object.get("_id").toString();
                String typ = object.get("_type").toString();
                Map source = (Map) object.get("_source");
                try {
                    rt.postForObject(esUrl + "/" + ".kibana/" + typ.trim() + "/" + id.trim(), objectMapper.writeValueAsString(source), Map.class, Collections.emptyMap());
                } catch (JsonProcessingException e) {
                    // TODO log.
                    e.printStackTrace();
                }
            });
        }
    }

    private boolean isIndexMissing(final RestTemplate restTemplate, final String indexName) {
        try {
            restTemplate.headForHeaders(esUrl + "/" + indexName);
        } catch (Exception ex) {
            return ex.getMessage().contains("404");
        }
        return false;
    }
}
