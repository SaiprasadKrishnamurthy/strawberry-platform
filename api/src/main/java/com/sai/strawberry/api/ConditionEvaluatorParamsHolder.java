package com.sai.strawberry.api;

import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.MappingManager;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Map;

@Data
@AllArgsConstructor
public class ConditionEvaluatorParamsHolder {
    private final MongoTemplate mongoTemplate;
    private final MongoTemplate mongoBatchDBTemplate;
    private final Session cassandraDBSession;
    private final MappingManager cassandraDataMappingManager;
    private final EventConfig eventConfig;
    private final Map eventEntityInAsMap;
}
