package com.sai.strawberry.api;

import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Map;

/**
 * @author Sai
 */
public abstract class MongoBackedDataTransformer implements CustomProcessorHook {
    public abstract Map process(EventConfig config, Map jsonIn, MongoTemplate slowZoneMongoTemplate, MongoTemplate fastZoneMongoTemplate);
}
