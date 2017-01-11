package com.strawberry.apps.jenkins;

import com.sai.strawberry.api.ConditionEvaluator;
import com.sai.strawberry.api.ConditionEvaluatorParamsHolder;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Map;

/**
 * Created by saipkri on 11/01/17.
 */
public class SlaveStatusShouldBeNotifiedCondition implements ConditionEvaluator {

    @Override
    public boolean test(final ConditionEvaluatorParamsHolder conditionEvaluatorParamsHolder) {
        // Check with last notified and then decide.
        System.out.println(" ---- condition to be evaluated before notification ------");
        Query query = new Query(Criteria.where("displayName")
                .is(conditionEvaluatorParamsHolder.getEventEntityInAsMap().get("displayName")));
        query.with(new Sort(Sort.Direction.DESC, "id")).limit(1);

        Map recentlyNotifiedEvent = conditionEvaluatorParamsHolder.getMongoTemplate().findOne(query, Map.class, conditionEvaluatorParamsHolder.getEventConfig().getConfigId());
        System.out.println(" Recently notified event: "+recentlyNotifiedEvent);
        if (recentlyNotifiedEvent != null && recentlyNotifiedEvent.get("lastNotifiedTimestamp") != null) {
            long currTimestamp = System.currentTimeMillis();
            long lastNotifiedTimestamp = (long) recentlyNotifiedEvent.get("lastNotifiedTimestamp");
            return (currTimestamp - lastNotifiedTimestamp) > (60 * 1000);
        } else {
            return true;
        }
    }
}
