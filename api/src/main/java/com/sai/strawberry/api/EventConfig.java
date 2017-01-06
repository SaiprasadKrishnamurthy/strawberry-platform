package com.sai.strawberry.api;

import lombok.*;

import java.util.Map;

/**
 * Created by saipkri on 11/11/16.
 */
@Data
public class EventConfig {
    private String configId;
    private String documentIdField;
    private boolean persistEvent;
    private boolean enableVisualization;
    private boolean enabled;
    private DataDefinitions dataDefinitions;
    private DataTransformation dataTransformation;
    private Notification notification;
    private Map<String, Object> internal;
    private String searchletClass;
    private String shouldAcceptConditionEvaluationClass;
}
