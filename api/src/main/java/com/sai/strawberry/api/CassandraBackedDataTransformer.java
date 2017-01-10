package com.sai.strawberry.api;

import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.MappingManager;

import java.util.List;
import java.util.Map;

/**
 * Created by saipkri on 18/12/16.
 */
public abstract class CassandraBackedDataTransformer {

    public abstract <T extends Object> List<T> entities(final Session dbSession, final EventConfig config, final Map eventEntityInAsMap);

    public abstract Map process(Session dbSession, MappingManager mappingManager,
                                EventConfig config,
                                Map eventEntityInAsMap);
}
