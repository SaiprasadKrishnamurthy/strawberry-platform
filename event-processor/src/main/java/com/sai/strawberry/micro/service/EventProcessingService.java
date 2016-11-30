package com.sai.strawberry.micro.service;

import akka.actor.ActorRef;
import akka.pattern.Patterns;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sai.strawberry.api.EventStreamConfig;
import com.sai.strawberry.micro.actor.*;
import com.sai.strawberry.micro.config.ActorFactory;
import com.sai.strawberry.micro.model.EventProcessingContext;
import org.springframework.stereotype.Component;
import scala.concurrent.Future;

import javax.inject.Inject;
import java.util.Map;

/**
 * Created by saipkri on 28/11/16.
 */
@Component
public class EventProcessingService {
    private final ActorFactory actorFactory;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Inject
    public EventProcessingService(final ActorFactory actorFactory) {
        this.actorFactory = actorFactory;
    }

    public void process(final String data) {
        try {
            System.out.println(data + " --> " + actorFactory);
            Map doc = MAPPER.readValue(data, Map.class);
            EventStreamConfig eventStreamConfig = MAPPER.convertValue(doc.get("eventStreamConfig"), EventStreamConfig.class);
            // Add the identifiers.

            Map payload = (Map) doc.get("payload");
            payload.put("__configId__", eventStreamConfig.getConfigId());
            payload.put("__naturalId__", payload.get(eventStreamConfig.getDocumentIdField()));


            EventProcessingContext context = new EventProcessingContext(payload, eventStreamConfig, System.currentTimeMillis());

            ActorRef persistenceActor = actorFactory.newActor(MongoPersistenceActor.class);
            ActorRef batchSetupActor = actorFactory.newActor(MongoBatchsetupActor.class);
            ActorRef kibanaActor = actorFactory.newActor(KibanaActor.class);
            ActorRef esIndexActor = actorFactory.newActor(ESIndexActor.class);

            ActorRef appCallbackActor = actorFactory.newActor(AppCallbackActor.class);
            ActorRef esPercolationActor = actorFactory.newActor(ESPercolationActor.class);
            ActorRef notificationActor = actorFactory.newActor(NotificationActor.class);
            ActorRef opsIndexActor = actorFactory.newActor(OpsIndexActor.class);

            // Full ASYNC one way.
            batchSetupActor.tell(context, ActorRef.noSender());
            persistenceActor.tell(context, ActorRef.noSender());
            kibanaActor.tell(context, ActorRef.noSender());
            esIndexActor.tell(context, ActorRef.noSender());

            // The below ones must be done in a sequence, but still async.
            Future<Object> appCallbackFuture = Patterns.ask(appCallbackActor, context, AppCallbackActor.timeout_in_seconds);
            Patterns.pipe(appCallbackFuture, actorFactory.executionContext())
                    .to(esPercolationActor);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
