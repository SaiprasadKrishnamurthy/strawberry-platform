package com.strawberry.apps.jenkins;

import com.sai.strawberry.api.ConditionEvaluatorParamsHolder;
import com.sai.strawberry.api.WebHooksUrlProvider;

import java.util.Arrays;
import java.util.List;

/**
 * Created by saipkri on 04/01/17.
 */
public class SlackNotificationUrlProvider implements WebHooksUrlProvider {

    @Override
    public List<String> webHookUrl(ConditionEvaluatorParamsHolder conditionEvaluatorParamsHolder) {
        return Arrays.asList("https://hooks.slack.com/services/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");
    }
}
