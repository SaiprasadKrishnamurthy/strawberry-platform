package com.strawberry.apps.jenkins;

import com.sai.strawberry.api.EventConfig;
import com.sai.strawberry.api.MongoBackedDataTransformer;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Map;

/**
 * Created by saipkri on 30/11/16.
 */
public class JenkinsSlaveEventTransformer extends MongoBackedDataTransformer {

    @Override
    public Map process(final EventConfig eventStreamConfig, final Map map, final MongoTemplate mongoTemplate, final MongoTemplate mongoTemplateBatch) {
        // nothing is required here.
        return map;
    }
}
