package com.sai.strawberry.micro.actor;

import akka.actor.UntypedActor;
import com.sai.strawberry.api.ConditionEvaluatorParamsHolder;
import com.sai.strawberry.micro.model.EventProcessingContext;
import com.sai.strawberry.micro.util.ConditionEvaluatorUtil;
import org.springframework.data.mongodb.core.CollectionOptions;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * Created by saipkri on 08/09/16.
 */
public class MongoBatchsetupActor extends UntypedActor {

    public static long timeout_in_seconds = 5 * 1000;

    private final MongoTemplate mongoTemplate;
    private final MongoTemplate mongoBatchTemplate;

    public MongoBatchsetupActor(final MongoTemplate mongoTemplate, final MongoTemplate mongoBatchTemplate) {
        this.mongoTemplate = mongoTemplate;
        this.mongoBatchTemplate = mongoBatchTemplate;
    }


    @Override
    public void onReceive(final Object message) throws Throwable {
        if (message instanceof EventProcessingContext) {
            EventProcessingContext context = (EventProcessingContext) message;
            if (context.getConfig().isPersistEvent() &&
                    context.getConfig().getDataDefinitions() != null
                    && context.getConfig().getDataDefinitions().getDatabase() != null
                    && context.getConfig().getDataDefinitions().getDatabase().getMongo() != null
                    && context.getConfig().getDataDefinitions().getDatabase().getMongo().getMaxBatchSizeInBytes() > 0
                    && ConditionEvaluatorUtil.test(context.getConfig().getDataDefinitions().getDatabase().getShouldPersistConditionEvaluationClass(), new ConditionEvaluatorParamsHolder(mongoTemplate, mongoBatchTemplate, null, null, context.getConfig(), context.getDoc()))) {
                if (!mongoBatchTemplate.collectionExists(context.getConfig().getConfigId())) {
                    CollectionOptions options = new CollectionOptions((int) context.getConfig().getDataDefinitions().getDatabase().getMongo().getMaxBatchSizeInBytes(), (int) context.getConfig().getDataDefinitions().getDatabase().getMongo().getMaxNumberOfDocsBatch(), true);
                    mongoBatchTemplate.createCollection(context.getConfig().getConfigId(), options);
                }
                mongoBatchTemplate.save(context.getDoc(), context.getConfig().getConfigId());
            }
        }
    }
}
