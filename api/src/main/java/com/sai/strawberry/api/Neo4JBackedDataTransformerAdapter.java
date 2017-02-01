package com.sai.strawberry.api;


import org.neo4j.ogm.session.Session;

import java.util.Map;

/**
 * Created by saipkri on 18/12/16.
 */
public abstract class Neo4JBackedDataTransformerAdapter extends Neo4JBackedDataTransformer {

    public Map process(final Session dbSession,
                       final EventConfig config,
                       final Map eventEntityInAsMap) {
        return eventEntityInAsMap;
    }
}
