package com.sai.strawberry.api;

import lombok.Data;

import java.util.Map;

/**
 * Created by saipkri on 13/12/16.
 */
@Data
public class SqlNotification {
    private String ddl;
    private Map<String, String> notificationChannelsAndQueries;
}
