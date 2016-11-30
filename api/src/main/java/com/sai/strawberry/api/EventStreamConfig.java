package com.sai.strawberry.api;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Created by saipkri on 11/11/16.
 */
@Data
public class EventStreamConfig {
    private String configId;
    private String documentIdField;
    private boolean cacheEnabled;
    private String customProcessingHookClassName;
    private String customProcessingHookScript;
    private boolean durableNotification;
    private Map<String, Object> indexDefinition;
    private Map<String, Map<String, Object>> watchQueries;
    private boolean persistEvent;
    private boolean enableVisualization;
    private BatchQueryConfig batchQueryConfig;
}
