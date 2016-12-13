package com.sai.app.banking.txn;

import com.sai.strawberry.api.EventConfig;
import com.sai.strawberry.api.MongoBackedDataTransformer;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by saipkri on 30/11/16.
 */
public class CardTxnDataTransformer extends MongoBackedDataTransformer {

    @Override
    public Map process(final EventConfig eventStreamConfig, final Map map, final MongoTemplate mongoTemplate, final MongoTemplate mongoTemplateBatch) {
        System.out.println(" \t\t Called my custom transformer: " + mongoTemplate);
        Map response = new HashMap<>();
        response.put("highValueTransaction", true);
        return response;
    }
}
