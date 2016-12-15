package com.sai.strawberry.micro.actor;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import com.sai.strawberry.api.EventConfig;
import com.sai.strawberry.api.NotificationConfig;
import com.sai.strawberry.micro.config.ActorFactory;
import com.sai.strawberry.micro.model.EventProcessingContext;
import com.sai.strawberry.micro.model.NotificationTuple;
import com.sai.strawberry.micro.model.ProcessorEvent;
import org.springframework.jdbc.core.JdbcTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.*;

/**
 * Created by saipkri on 08/09/16.
 */
public class WatcherSQLDBActor extends UntypedActor {

    private final JdbcTemplate jdbcTemplate;
    private final ActorFactory actorFactory;
    private SimpleDateFormat format = new SimpleDateFormat("YYYY-MM-dd'T'HH:mm:ss");


    public WatcherSQLDBActor(final JdbcTemplate jdbcTemplate, final ActorFactory actorFactory) {
        this.jdbcTemplate = jdbcTemplate;
        this.actorFactory = actorFactory;
    }

    @Override
    public void onReceive(final Object _context) throws Throwable {
        if (_context instanceof EventProcessingContext) {
            EventProcessingContext context = (EventProcessingContext) _context;
            EventConfig config = context.getConfig();
            if (config.getNotification() != null
                    && config.getNotification().getSql() != null
                    && config.getNotification().getSql().getDdl() != null) {
                // create the table first.
                jdbcTemplate.execute(config.getNotification().getSql().getDdl().trim());

                // insert the current row.
                List<String> columns = (List<String>) context.getConfig().getInternal().get("sqlColNames");
                String tableName = context.getConfig().getInternal().get("tableName").toString();
                String colNames = " (" + columns.stream().collect(joining(",")) + ") ";
                String colValuesPlaceHolders = " (" + columns.stream().map(str -> "?").collect(joining(",")) + ") ";
                String insertSql = "INSERT INTO " + tableName + colNames + "VALUES" + colValuesPlaceHolders;
                Map _doc = context.getDoc();
                Map doc = (Map) _doc.keySet()
                        .stream()
                        .collect(toMap(key -> key.toString().toUpperCase(), key -> _doc.get(key)));
                List<Object> params = columns.stream().map(col -> doc.get(col.toUpperCase())).collect(toList());
                jdbcTemplate.update(insertSql, params.toArray(new Object[params.size()]));

                List<String> notifiedChannels = new ArrayList<>();
                ActorRef watcherSQLDBCleanupActor = actorFactory.newActor(WatcherSQLDBCleanupActor.class);

                if (context.getConfig().getNotification() != null
                        && context.getConfig().getNotification().getSql() != null
                        && context.getConfig().getNotification().getSql().getNotificationConfigs() != null
                        && !context.getConfig().getNotification().getSql().getNotificationConfigs().isEmpty()) {
                    // Run the watcher query now.
                    for (NotificationConfig sqlEntry : context.getConfig().getNotification().getSql().getNotificationConfigs()) {
                        String channelName = sqlEntry.getChannelName();
                        String sql = sqlEntry.getSqlQuery();
                        List result = jdbcTemplate.queryForList(sql);
                        if (result != null && !result.isEmpty()) {
                            // SEND IT TO NOTIFICATION ACTOR.
                            ActorRef notificationActor = actorFactory.newActor(NotificationActor.class);
                            notifiedChannels.add(channelName.trim());
                            notificationActor.tell(new NotificationTuple(context, channelName), getSelf());
                        }
                        // Async delete
                        watcherSQLDBCleanupActor.tell(context, getSelf());
                    }

                    // Construct an event now.
                    // SEND IT TO OPS ACTOR.
                    ActorRef opsIndexActor = actorFactory.newActor(OpsIndexActor.class);
                    ProcessorEvent event = new ProcessorEvent();
                    event.setStreamId(context.getConfig().getConfigId());
                    event.setStartTime(context.getStartTimestamp());
                    event.setOriginatedTimestamp(format.format(new Date()));
                    event.setProcessingTimeInMillis(System.currentTimeMillis() - context.getStartTimestamp());
                    event.setNotifiedTo(notifiedChannels.toArray(new String[notifiedChannels.size()]));
                    event.setPayloadIdentifier(context.getDoc().get("__naturalId__").toString());
                    opsIndexActor.tell(event, getSelf());
                }
            }
        }
        getSender().tell(_context, getSelf());
    }
}
