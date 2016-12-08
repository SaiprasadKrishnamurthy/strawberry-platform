package com.sai.strawberry.micro.actor;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import com.sai.strawberry.micro.config.ActorFactory;
import com.sai.strawberry.micro.model.EventProcessingContext;
import com.sai.strawberry.micro.model.NotificationTuple;
import com.sai.strawberry.micro.model.ProcessorEvent;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

/**
 * Created by saipkri on 08/09/16.
 */
public class WatcherSQLDBCleanupActor extends UntypedActor {

    private final JdbcTemplate jdbcTemplate;
    private final ActorFactory actorFactory;
    private SimpleDateFormat format = new SimpleDateFormat("YYYY-MM-dd'T'HH:mm:ss");



    public WatcherSQLDBCleanupActor(final JdbcTemplate jdbcTemplate, final ActorFactory actorFactory) {
        this.jdbcTemplate = jdbcTemplate;
        this.actorFactory = actorFactory;
    }

    @Override
    public void onReceive(final Object _context) throws Throwable {
        if (_context instanceof EventProcessingContext) {
            EventProcessingContext context = (EventProcessingContext) _context;
            if (StringUtils.hasText(context.getConfig().getSqlDDL().trim())) {
                // create the table first.
                jdbcTemplate.execute(context.getConfig().getSqlDDL().trim());

                // insert the current row.
                List<String> columns = (List<String>) context.getConfig().getInternal().get("sqlColNames");
                String tableName = context.getConfig().getInternal().get("tableName").toString();
                String colNames = " (" + columns.stream().collect(joining(",")) + ") ";
                String colValuesPlaceHolders = " (" + columns.stream().map(str -> "?").collect(joining(",")) + ") ";
                String insertSql = "INSERT INTO " + tableName + colNames + "VALUES" + colValuesPlaceHolders;
                Map doc = context.getDoc();
                List<Object> params = columns.stream().map(col -> doc.get(col)).collect(toList());
                jdbcTemplate.update(insertSql, params.toArray(new Object[params.size()]));

                List<String> notifiedChannels = new ArrayList<>();

                // Run the watcher query now.
                for (Map.Entry<String, String> sqlEntry : context.getConfig().getWatchQueriesSql().entrySet()) {
                    String channelName = sqlEntry.getKey();
                    String sql = sqlEntry.getValue();
                    List result = jdbcTemplate.queryForList(sql);
                    if (result != null && !result.isEmpty()) {
                        // SEND IT TO NOTIFICATION ACTOR.
                        ActorRef notificationActor = actorFactory.newActor(NotificationActor.class);
                        notifiedChannels.add(channelName.trim());
                        notificationActor.tell(new NotificationTuple(context, channelName), getSelf());


                    }
                }

                // Construct an event now.
                // SEND IT TO OPS ACTOR.
                ActorRef opsIndexActor = actorFactory.newActor(OpsIndexActor.class);
                ProcessorEvent event = new ProcessorEvent();
                event.setStreamId(context.getConfig().getConfigId());
                event.setOriginatedTimestamp(format.format(new Date()));
                event.setProcessingTimeInMillis(System.currentTimeMillis() - event.getStartTime());
                event.setNotifiedTo(notifiedChannels.toArray(new String[notifiedChannels.size()]));
                event.setPayloadIdentifier(context.getDoc().get("__naturalId__").toString());
                opsIndexActor.tell(event, getSelf());
            }
        }
        getSender().tell(_context, getSelf());
    }
}
