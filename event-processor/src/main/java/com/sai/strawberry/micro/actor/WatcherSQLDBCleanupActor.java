package com.sai.strawberry.micro.actor;

import akka.actor.UntypedActor;
import com.sai.strawberry.micro.model.EventProcessingContext;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Created by saipkri on 08/09/16.
 */
public class WatcherSQLDBCleanupActor extends UntypedActor {

    private final JdbcTemplate jdbcTemplate;


    public WatcherSQLDBCleanupActor(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void onReceive(final Object _context) throws Throwable {
        if (_context instanceof EventProcessingContext) {
            EventProcessingContext context = (EventProcessingContext) _context;
            if (context.getConfig().getNotification() != null
                    && context.getConfig().getNotification().getSql() != null
                    && context.getConfig().getNotification().getSql().getDdl() != null) {
                // create the table first.
                String tableName = context.getConfig().getInternal().get("tableName").toString();
                jdbcTemplate.update("DELETE FROM " + tableName + " WHERE " + context.getConfig().getDocumentIdField() + " = " + context.getDoc().get(context.getConfig().getDocumentIdField()));
            }
        }
    }
}
