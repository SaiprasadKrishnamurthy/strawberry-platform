package com.sai.strawberry.micro.actor;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.MappingManager;
import com.sai.strawberry.api.ConditionEvaluatorParamsHolder;
import com.sai.strawberry.api.EventConfig;
import com.sai.strawberry.micro.config.ActorFactory;
import com.sai.strawberry.micro.model.EventProcessingContext;
import com.sai.strawberry.micro.util.ConditionEvaluatorUtil;
import org.springframework.data.mongodb.core.MongoTemplate;

public class PreNotificationChecksActor extends UntypedActor {

    private final ActorFactory actorFactory;
    private final MongoTemplate mongoTemplate;
    private final MongoTemplate mongoBatchTemplate;
    private final Session cassandraSession;
    private final MappingManager cassandraMappingManager;


    public PreNotificationChecksActor(final ActorFactory actorFactory, final MongoTemplate mongoTemplate, final MongoTemplate mongoBatchTemplate, final Session cassandraSession, final MappingManager cassandraMappingManager) {
        this.actorFactory = actorFactory;
        this.mongoTemplate = mongoTemplate;
        this.mongoBatchTemplate = mongoBatchTemplate;
        this.cassandraSession = cassandraSession;
        this.cassandraMappingManager = cassandraMappingManager;
    }

    @Override
    public void onReceive(final Object _context) throws Throwable {
        if (_context instanceof EventProcessingContext) {
            ActorRef esPercolationActor = actorFactory.newActor(ESPercolationActor.class);
            ActorRef watcherSqlActor = actorFactory.newActor(WatcherSQLDBActor.class);
            ActorRef spelExpressionEvaluationActor = actorFactory.newActor(SpelExpressionEvaluationActor.class);

            EventProcessingContext context = (EventProcessingContext) _context;
            EventConfig config = context.getConfig();
            if (config.getNotification() != null
                    && ConditionEvaluatorUtil.test(config.getNotification().getShouldBeConsideredForNotificationConditionEvaluationClass(), new ConditionEvaluatorParamsHolder(mongoTemplate, mongoBatchTemplate, cassandraSession, cassandraMappingManager, config, context.getDoc()))) {
                getSender().tell(_context, getSelf());
                esPercolationActor.tell(_context, getSender());
                watcherSqlActor.tell(_context, getSender());
                spelExpressionEvaluationActor.tell(_context, getSender());
            } else {
                System.out.println(" ----- Notification consideration condition evaluated to false. Therefore nothing to notify. ---- ");
            }
        }
    }
}
