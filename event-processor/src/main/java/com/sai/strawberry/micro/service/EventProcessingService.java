package com.sai.strawberry.micro.service;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.pattern.Patterns;
import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.MappingManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sai.strawberry.api.ConditionEvaluatorParamsHolder;
import com.sai.strawberry.api.EventConfig;
import com.sai.strawberry.micro.actor.*;
import com.sai.strawberry.micro.config.ActorFactory;
import com.sai.strawberry.micro.model.EventProcessingContext;
import com.sai.strawberry.micro.util.ConditionEvaluatorUtil;
import org.springframework.data.mongodb.core.MongoTemplate;
import scala.concurrent.Future;

import java.util.Map;

/**
 * Created by saipkri on 28/11/16.
 */
public class EventProcessingService extends UntypedActor {
    private final MongoTemplate mongoTemplate;
    private final MongoTemplate mongoBatchDB;
    private final Session cassandraSession;
    private final MappingManager cassandraMappingManager;
    private final ActorFactory actorFactory;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public EventProcessingService(final MongoTemplate mongoTemplate, final MongoTemplate mongoBatchDB,
                                  final Session cassandraSession, final MappingManager cassandraMappingManager,
                                  final ActorFactory actorFactory) {
        this.mongoTemplate = mongoTemplate;
        this.mongoBatchDB = mongoBatchDB;
        this.cassandraSession = cassandraSession;
        this.cassandraMappingManager = cassandraMappingManager;
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

            // First check if this event should be considered at all.
            ConditionEvaluatorParamsHolder conditionEvaluatorParamsHolder = new ConditionEvaluatorParamsHolder(mongoTemplate, mongoBatchDB, cassandraSession, cassandraMappingManager, eventStreamConfig, doc);

            System.out.println(" Entered here.... ");
            if (ConditionEvaluatorUtil.test(eventStreamConfig.getShouldAcceptConditionEvaluationClass(), conditionEvaluatorParamsHolder)) {
                System.out.println(" Entered here.... 1");
                System.out.println(" Entered here.... 2");


                // Add the identifiers.
                Map payload = (Map) doc.get("payload");
                long timestamp = (long) doc.get("timestamp");
                payload.put("__configId__", eventStreamConfig.getConfigId());
                payload.put("__naturalId__", payload.get(eventStreamConfig.getDocumentIdField()));

                EventProcessingContext context = new EventProcessingContext(payload, eventStreamConfig, timestamp);

                ActorRef persistenceActor = actorFactory.newActor(MongoPersistenceActor.class);
                ActorRef batchSetupActor = actorFactory.newActor(MongoBatchsetupActor.class);
                ActorRef kibanaActor = actorFactory.newActor(KibanaActor.class);

                ActorRef appCallbackActor = actorFactory.newActor(AppCallbackActor.class);
                ActorRef preNotificationChecksActor = actorFactory.newActor(PreNotificationChecksActor.class);

                // Full ASYNC one way.
                batchSetupActor.tell(context, ActorRef.noSender());
                persistenceActor.tell(context, ActorRef.noSender());
                kibanaActor.tell(context, ActorRef.noSender());

                // The below ones must be done in a sequence, but still async.
                Future<Object> appCallbackFuture = Patterns.ask(appCallbackActor, context, AppCallbackActor.timeout_in_seconds);
                Patterns.pipe(appCallbackFuture, actorFactory.executionContext())
                        .to(preNotificationChecksActor);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
