package com.sai.strawberry.api;

import lombok.Data;

import java.util.Map;

/**
 * Created by saipkri on 13/12/16.
 */
@Data
public class ElasticsearchNotification {
    private Map<String, Map<String, Object>> notificationChannelsAndQueries;
}
