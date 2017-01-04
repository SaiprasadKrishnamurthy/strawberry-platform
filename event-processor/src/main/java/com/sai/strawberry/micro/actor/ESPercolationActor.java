package com.sai.strawberry.micro.actor;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sai.strawberry.micro.config.ActorFactory;
import com.sai.strawberry.micro.model.EventProcessingContext;
import com.sai.strawberry.micro.model.NotificationTuple;
import com.sai.strawberry.micro.model.ProcessorEvent;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by saipkri on 08/09/16.
 */
public class ESPercolationActor extends UntypedActor {

    private final String esUrl;
    private final ActorFactory actorFactory;
    private ObjectMapper objectMapper = new ObjectMapper();
    private SimpleDateFormat format = new SimpleDateFormat("YYYY-MM-dd'T'HH:mm:ss");


    public ESPercolationActor(final String esUrl, final ActorFactory actorFactory) {
        this.esUrl = esUrl;
        this.actorFactory = actorFactory;
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    @Override
    public void onReceive(final Object message) throws Throwable {
        if (message instanceof EventProcessingContext) {
            EventProcessingContext context = (EventProcessingContext) message;
            if (context.getConfig().getNotification() != null
                    && context.getConfig().getNotification().getElasticsearch() != null
                    && context.getConfig().getNotification().getElasticsearch().getNotificationConfigs() != null
                    && !context.getConfig().getNotification().getElasticsearch().getNotificationConfigs().isEmpty()) {
                RestTemplate rt = new RestTemplate();
                String topic = context.getConfig().getConfigId();

                // Percolate the query.
                Map docTobePercolated = new HashMap<>();
                docTobePercolated.put("doc", context.getDoc());

                Map percolationResponse = rt.postForObject(esUrl + "/" + topic + "/" + topic + "/_percolate", objectMapper.writeValueAsString(docTobePercolated), Map.class, Collections.emptyMap());
                List<Map> matches = (List<Map>) percolationResponse.get("matches");

                List<String> notifiedChannels = new ArrayList<>();

                // Get the individual matched query
                for (Map matchedQuery : matches) {

                    // SEND IT TO NOTIFICATION ACTOR.
                    ActorRef notificationActor = actorFactory.newActor(NotificationActor.class);

                    String queryId = matchedQuery.get("_id").toString();
                    Map percolationQueryObject = rt.getForObject(esUrl + "/" + topic + "/.percolator/" + queryId, Map.class);
                    String queryName = ((Map) percolationQueryObject.get("_source")).get("queryName").toString();

                    notifiedChannels.add(queryName.trim());
                    notificationActor.tell(new NotificationTuple(context, queryName), getSelf());
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
        getSender().tell(message, getSelf());
    }
}
