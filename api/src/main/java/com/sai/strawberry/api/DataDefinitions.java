package com.sai.strawberry.api;

import lombok.Data;

import java.util.Map;

/**
 * Created by saipkri on 13/12/16.
 */
@Data
public class DataDefinitions {
    private Map<String, Object> elasticsearchIndexSettings;
    private Map<String, Object> elasticsearchIndexDefinition;
    private Database database;
}
