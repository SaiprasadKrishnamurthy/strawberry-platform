package com.sai.strawberry.api;

import lombok.Data;

import java.util.Map;

/**
 * Created by saipkri on 14/12/16.
 */
@Data
public class NotificationConfig {
    private String channelName;
    private Map<String, Object> elasticsearchQuery;
    private String sqlQuery;
    private String webhookUrl;
    private String webHookDataTransformerClass;
}