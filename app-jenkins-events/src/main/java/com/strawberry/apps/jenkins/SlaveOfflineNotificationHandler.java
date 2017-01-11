package com.strawberry.apps.jenkins;

import com.sai.strawberry.api.ConditionEvaluatorParamsHolder;
import com.sai.strawberry.api.Handler;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by saipkri on 11/01/17.
 */
public class SlaveOfflineNotificationHandler implements Handler {

    @Override
    public Map<String, Object> handle(final ConditionEvaluatorParamsHolder conditionEvaluatorParamsHolder) {
        System.out.println(" ------- HANDLER CALLED DURING NOTIFICATION ------ ");

        // TODO save last notified timestamp as the current timestamp.
        // Find doc by id and update it with adding the notifiedTimestamp.

        Query query = new Query(Criteria.where("id").is(conditionEvaluatorParamsHolder.getEventEntityInAsMap().get("id")));
        conditionEvaluatorParamsHolder.getMongoTemplate().remove(query, conditionEvaluatorParamsHolder.getEventConfig().getConfigId());

        // at this point the event has been removed from the db.
        conditionEvaluatorParamsHolder.getEventEntityInAsMap().put("lastNotifiedTimestamp", System.currentTimeMillis());

        // saved back in mongo.
        conditionEvaluatorParamsHolder.getMongoTemplate().save(conditionEvaluatorParamsHolder.getEventEntityInAsMap(), conditionEvaluatorParamsHolder.getEventConfig().getConfigId());
        return conditionEvaluatorParamsHolder.getEventEntityInAsMap();
    }
}
