package com.sai.strawberry.micro.actor;

import akka.actor.UntypedActor;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.sai.strawberry.api.EventConfig;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by saipkri on 08/09/16.
 */
public class CassandraDDLSetupActor extends UntypedActor {

    public static long timeout_in_seconds = 5 * 1000;
    private final Cluster cassandraCluster;

    public CassandraDDLSetupActor(final Cluster cassandraCluster) {
        this.cassandraCluster = cassandraCluster;
    }

    @Override
    public void onReceive(final Object _config) throws Throwable {
        EventConfig config = null;

        if (_config instanceof EventConfig) {
            config = (EventConfig) _config;
            try (Session session = cassandraCluster.connect()) {
                if(config.getDataDefinitions().getDatabase() != null && config.getDataDefinitions().getDatabase().getCassandra() != null) {
                    config.getDataDefinitions().getDatabase().getCassandra().getCassandraDDLs().stream()
                            .forEach(session::execute);
                }
            }
        }
        getSender().tell(_config, getSelf());
    }
}
