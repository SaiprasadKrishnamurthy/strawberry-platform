package com.sai.strawberry.api;

import lombok.Data;

/**
 * Created by saipkri on 13/12/16.
 */
@Data
public class Notification {
    private String webhooksUrl;
    private ElasticsearchNotification elasticsearch;
    private SqlNotification sql;
    private SpelNotification spel;
}
