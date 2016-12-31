package com.sai.strawberry.api;

import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.MappingManager;

import java.util.Map;

/**
 * Created by saipkri on 18/12/16.
 */
public abstract class CassandraBackedDataTransformer {

    public abstract Map process(Session dbSession, MappingManager mappingManager,
                                EventConfig config,
                                Map eventEntityInAsMap);
}
