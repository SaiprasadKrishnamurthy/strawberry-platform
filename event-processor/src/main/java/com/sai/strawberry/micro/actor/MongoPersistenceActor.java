package com.sai.strawberry.micro.actor;

import akka.actor.UntypedActor;
import com.sai.strawberry.api.ConditionEvaluatorParamsHolder;
import com.sai.strawberry.micro.model.EventProcessingContext;
import com.sai.strawberry.micro.util.ConditionEvaluatorUtil;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

/**
 * Created by saipkri on 08/09/16.
 */
public class MongoPersistenceActor extends UntypedActor {

    public static long timeout_in_seconds = 5 * 1000;

    private final MongoTemplate mongoTemplate;
    private final MongoTemplate mongoTemplateBatch;

    public MongoPersistenceActor(final MongoTemplate mongoTemplate, final MongoTemplate mongoTemplateBatch) {
        this.mongoTemplate = mongoTemplate;
        this.mongoTemplateBatch = mongoTemplateBatch;
    }


    @Override
    public void onReceive(final Object message) throws Throwable {
        if (message instanceof EventProcessingContext) {
            EventProcessingContext context = (EventProcessingContext) message;
            if (context.getConfig().isPersistEvent()
                    && context.getConfig().getDataDefinitions().getDatabase() != null
                    && context.getConfig().getDataDefinitions().getDatabase().getMongo() != null
                    && ConditionEvaluatorUtil.test(context.getConfig().getDataDefinitions().getDatabase().getShouldPersistConditionEvaluationClass(), new ConditionEvaluatorParamsHolder(mongoTemplate, mongoTemplateBatch, null, null, context.getConfig(), context.getDoc()))) {
                if (!context.getConfig().getDataDefinitions().getDatabase().getMongo().isUpsertMode()) {
                    mongoTemplate.save(context.getDoc(), context.getConfig().getConfigId());
                } else {
                    String uniqueIdField = context.getConfig().getDocumentIdField();
                    Query query = new Query();
                    query.addCriteria(Criteria.where(uniqueIdField.trim()).is(context.getDoc().get(uniqueIdField.trim())));
                    mongoTemplate.remove(query, context.getConfig().getConfigId());
                    mongoTemplate.save(context.getDoc(), context.getConfig().getConfigId());
                }
            }
        }
    }
}
