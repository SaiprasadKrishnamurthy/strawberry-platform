package com.sai.strawberry.api;


import org.neo4j.ogm.session.Session;

import java.util.List;
import java.util.Map;

/**
 * Created by saipkri on 18/12/16.
 */
public abstract class Neo4JBackedDataTransformer {

    public abstract <T extends Object> List<T> entities(final Session dbSession, final EventConfig config, final Map eventEntityInAsMap);

    public abstract Map process(Session dbSession,
                                EventConfig config,
                                Map eventEntityInAsMap);
}
