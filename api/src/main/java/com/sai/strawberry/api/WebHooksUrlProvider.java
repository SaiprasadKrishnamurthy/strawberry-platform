package com.sai.strawberry.api;

import java.util.List;

public interface WebHooksUrlProvider {
    List<String> webHookUrl(final ConditionEvaluatorParamsHolder conditionEvaluatorParamsHolder);
}
