package com.sai.strawberry.micro.service;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.pattern.Patterns;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sai.strawberry.api.EventConfig;
import com.sai.strawberry.micro.actor.*;
import com.sai.strawberry.micro.config.ActorFactory;
import com.sai.strawberry.micro.model.EventProcessingContext;
import scala.concurrent.Future;

import java.util.Map;

/**
 * Created by saipkri on 28/11/16.
 */
public class EventProcessingService extends UntypedActor {
    private final ActorFactory actorFactory;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public EventProcessingService(final ActorFactory actorFactory) {
        this.actorFactory = actorFactory;
    }

    @Override
    public void onReceive(Object message) throws Throwable {
        if (message instanceof String) {
            process((String) message);
        }
    }

    private void process(final String data) {
        try {
            Map doc = MAPPER.readValue(data, Map.class);
            EventConfig eventStreamConfig = MAPPER.convertValue(doc.get("eventStreamConfig"), EventConfig.class);
            // Add the identifiers.

            Map payload = (Map) doc.get("payload");
            long timestamp = (long) doc.get("timestamp");
            payload.put("__configId__", eventStreamConfig.getConfigId());
            payload.put("__naturalId__", payload.get(eventStreamConfig.getDocumentIdField()));

            EventProcessingContext context = new EventProcessingContext(payload, eventStreamConfig, timestamp);

            ActorRef persistenceActor = actorFactory.newActor(MongoPersistenceActor.class);
            ActorRef batchSetupActor = actorFactory.newActor(MongoBatchsetupActor.class);
            ActorRef kibanaActor = actorFactory.newActor(KibanaActor.class);
            ActorRef esIndexActor = actorFactory.newActor(ESIndexActor.class);

            ActorRef appCallbackActor = actorFactory.newActor(AppCallbackActor.class);
            ActorRef esPercolationActor = actorFactory.newActor(ESPercolationActor.class);
            ActorRef watcherSqlDbSetupActor = actorFactory.newActor(WatcherSQLDBActor.class);

            // Full ASYNC one way.
            batchSetupActor.tell(context, ActorRef.noSender());
            persistenceActor.tell(context, ActorRef.noSender());
            kibanaActor.tell(context, ActorRef.noSender());
            esIndexActor.tell(context, ActorRef.noSender());

            // The below ones must be done in a sequence, but still async.
            Future<Object> appCallbackFuture = Patterns.ask(appCallbackActor, context, AppCallbackActor.timeout_in_seconds);
            Patterns.pipe(appCallbackFuture, actorFactory.executionContext())
                    .to(esPercolationActor)
                    .to(watcherSqlDbSetupActor);


        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
