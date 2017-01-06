package com.sai.strawberry.micro.util;

import com.sai.strawberry.api.ConditionEvaluator;
import com.sai.strawberry.api.ConditionEvaluatorParamsHolder;
import org.apache.commons.lang.StringUtils;

public final class ConditionEvaluatorUtil {

    private ConditionEvaluatorUtil() {
    }

    public static boolean test(final String conditionEvaluatorClass, final ConditionEvaluatorParamsHolder conditionEvaluatorParamsHolder) {
        if (StringUtils.isNotBlank(conditionEvaluatorClass)) {
            try {
                ConditionEvaluator conditionEvaluator = (ConditionEvaluator) Class.forName(conditionEvaluatorClass).newInstance();
                return conditionEvaluator.test(conditionEvaluatorParamsHolder);
            } catch (Exception ex) {
                ex.printStackTrace();
                return false;
            }
        }
        return true;
    }
}
