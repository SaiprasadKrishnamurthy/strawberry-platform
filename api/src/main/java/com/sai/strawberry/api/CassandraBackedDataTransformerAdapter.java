package com.sai.strawberry.api;

import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.MappingManager;

import java.util.Map;

/**
 * Created by saipkri on 18/12/16.
 */
public abstract class CassandraBackedDataTransformerAdapter extends CassandraBackedDataTransformer {

    public Map process(final Session dbSession, final MappingManager mappingManager,
                       final EventConfig config,
                       final Map eventEntityInAsMap) {
        return eventEntityInAsMap;
    }
}
