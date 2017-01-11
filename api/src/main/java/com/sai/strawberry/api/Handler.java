package com.sai.strawberry.api;

import java.util.Map;

/**
 * Created by saipkri on 11/01/17.
 */
public interface Handler {
    Map<String, Object> handle(ConditionEvaluatorParamsHolder params);
}
