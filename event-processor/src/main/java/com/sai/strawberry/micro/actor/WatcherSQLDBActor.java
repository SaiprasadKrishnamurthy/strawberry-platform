package com.sai.strawberry.micro.actor;

import akka.actor.UntypedActor;
import com.sai.strawberry.api.EventStreamConfig;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;

/**
 * Created by saipkri on 08/09/16.
 */
public class WatcherSQLDBActor extends UntypedActor {

    private final JdbcTemplate jdbcTemplate;

    public WatcherSQLDBActor(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void onReceive(final Object _config) throws Throwable {
        if (_config instanceof EventStreamConfig) {
            EventStreamConfig config = (EventStreamConfig) _config;
            if (StringUtils.hasText(config.getSqlDDL().trim())) {
                jdbcTemplate.execute(config.getSqlDDL().trim());
            }
        }
        getSender().tell(_config, getSelf());
    }
}
