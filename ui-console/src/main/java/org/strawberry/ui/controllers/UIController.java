package org.strawberry.ui.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.springframework.web.client.RestTemplate;
import org.strawberry.ui.controllers.model.EventStreamConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by saipkri on 08/12/16.
 */
@Data
public class UIController {

    // TODO config.
    private static final String configsEndpoint = "http://192.168.99.100:9999/config";
    private static final String dashboardEndpoint = "http://192.168.99.100:9999/ops-dashboard-link";
    private List<EventStreamConfig> configs = new ArrayList<>();

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper om = new ObjectMapper();
    private String dashboard;

    public UIController() {
        configs.clear();
        List _configs = restTemplate.getForObject(configsEndpoint, List.class);
        _configs.forEach(res -> {
            com.sai.strawberry.api.EventStreamConfig t = om.convertValue(res, com.sai.strawberry.api.EventStreamConfig.class);
            configs.add(new EventStreamConfig(t));
        });
        dashboard = restTemplate.getForObject(dashboardEndpoint, String.class);
    }
}
