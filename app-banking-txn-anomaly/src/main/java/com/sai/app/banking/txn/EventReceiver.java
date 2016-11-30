package com.sai.app.banking.txn;

import com.sai.strawberry.api.CustomProcessorHook;
import com.sai.strawberry.api.EventStreamConfig;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by saipkri on 30/11/16.
 */
public class EventReceiver implements CustomProcessorHook {

    @Override
    public Map process(final EventStreamConfig eventStreamConfig, final Map map, final MongoTemplate mongoTemplate, final MongoTemplate mongoTemplateBatch) {
        System.out.println(" \t\t Called my custom processor hook: " + mongoTemplate);
        Map response = new HashMap<>();
        response.put("highValueTransaction", true);
        return response;
    }
}
