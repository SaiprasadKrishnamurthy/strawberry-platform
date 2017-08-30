package com.sai.strawberry.micro.actor;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import com.sai.strawberry.api.EventConfig;
import com.sai.strawberry.api.NotificationConfig;
import com.sai.strawberry.micro.config.ActorFactory;
import com.sai.strawberry.micro.model.EventProcessingContext;
import com.sai.strawberry.micro.model.NotificationTuple;
import com.sai.strawberry.micro.model.ProcessorEvent;
import com.sai.strawberry.micro.util.SpelExpressionCache;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by saipkri on 08/09/16.
 */
public class SpelExpressionEvaluationActor extends UntypedActor {

    private final ActorFactory actorFactory;
    private SimpleDateFormat format = new SimpleDateFormat("YYYY-MM-dd'T'HH:mm:ss");


    public SpelExpressionEvaluationActor(final ActorFactory actorFactory) {
        this.actorFactory = actorFactory;
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    @Override
    public void onReceive(final Object _context) throws Throwable {
        if (_context instanceof EventProcessingContext) {
            EventProcessingContext context = (EventProcessingContext) _context;
            EventConfig config = context.getConfig();
            if (config.getNotification() != null
                    && config.getNotification().getSpel() != null) {

                List<String> notifiedChannels = new ArrayList<>();

                if (context.getConfig().getNotification() != null
                        && context.getConfig().getNotification().getSpel() != null
                        && context.getConfig().getNotification().getSpel().getNotificationConfigs() != null
                        && !context.getConfig().getNotification().getSpel().getNotificationConfigs().isEmpty()) {
                    // Run the watcher query now.
                    for (NotificationConfig spelEntry : context.getConfig().getNotification().getSpel().getNotificationConfigs()) {
                        String channelName = spelEntry.getChannelName();
                        String spel = spelEntry.getSpelExpressionQuery();
                        Expression expression = SpelExpressionCache.compiledExpression(spel.trim());
                        StandardEvaluationContext spelEvalContext = new StandardEvaluationContext(context.getDoc());
                        if (expression.getValue(spelEvalContext, Boolean.class)) {
                            // SEND IT TO NOTIFICATION ACTOR.
                            ActorRef notificationActor = actorFactory.newActor(NotificationActor.class);
                            notifiedChannels.add(channelName.trim());
                            notificationActor.tell(new NotificationTuple(context, channelName, spelEntry), getSelf());
                        }
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
