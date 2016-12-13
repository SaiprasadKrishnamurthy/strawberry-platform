package com.sai.strawberry.micro.actor;

import akka.actor.UntypedActor;
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
public class WatcherSQLDBSetupActor extends UntypedActor {

    public static long timeout_in_seconds = 5 * 1000;
    private final JdbcTemplate jdbcTemplate;

    public WatcherSQLDBSetupActor(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void onReceive(final Object _config) throws Throwable {
        EventConfig config = null;
        if (_config instanceof EventConfig) {
            config = (EventConfig) _config;
            if (config.getNotification() != null
                    && config.getNotification().getSql() != null
                    && config.getNotification().getSql().getDdl() != null) {
                // create the table first.
                jdbcTemplate.execute(config.getNotification().getSql().getDdl().trim());
                config.setInternal(tableMetadata(jdbcTemplate, config));
            }
        }
        getSender().tell(_config, getSelf());
    }

    private Map<String, Object> tableMetadata(final JdbcTemplate jdbcTemplate, final EventConfig eventStreamConfig) {
        Map<String, Object> internal = new HashMap<>();
        List<String> cols = new ArrayList<>();
        jdbcTemplate.query("select * from " + eventStreamConfig.getConfigId().trim(), rs -> {
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                String tableName = rsmd.getTableName(i);
                internal.put("tableName", tableName);
                cols.add(rsmd.getColumnName(i));
            }
            return columnCount;
        });
        internal.put("sqlColNames", cols);
        return internal;
    }
}
