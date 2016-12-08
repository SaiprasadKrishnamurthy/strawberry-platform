package com.sai.strawberry.micro.actor;

import akka.actor.UntypedActor;
import com.sai.strawberry.api.EventStreamConfig;
import com.sai.strawberry.micro.model.EventProcessingContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;

/**
 * Created by saipkri on 08/09/16.
 */
public class WatcherSQLDBSetupActor extends UntypedActor {

    private final JdbcTemplate jdbcTemplate;

    public WatcherSQLDBSetupActor(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void onReceive(final Object _context) throws Throwable {
        if (_context instanceof EventProcessingContext) {
            EventStreamConfig context = (EventStreamConfig) _context;
            if (StringUtils.hasText(context.getSqlDDL().trim())) {
                // create the table first.
                jdbcTemplate.execute(context.getSqlDDL().trim());

            }
        }
        getSender().tell(_context, getSelf());
    }
}
