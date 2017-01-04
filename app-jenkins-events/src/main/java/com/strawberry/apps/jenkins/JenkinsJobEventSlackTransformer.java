package com.strawberry.apps.jenkins;

import com.sai.strawberry.api.Callback;

import java.util.Map;

/**
 * Created by saipkri on 04/01/17.
 */
public class JenkinsJobEventSlackTransformer implements Callback {
    @Override
    public String call(final Map eventJson) {
        StringBuilder out = new StringBuilder();
        out.append("*").append(eventJson.get("jobName")).append(" has failed in Jenkins!*\n");
        out.append("```");
        out.append("Build number:  ").append(eventJson.get("buildNumber")).append("\n");
        out.append("Job url:  ").append(eventJson.get("jobUrl")).append("\n");
        out.append("Job start time:  ").append(eventJson.get("startTime")).append("\n");
        out.append("Job duration in minutes:  ").append((Long.parseLong(eventJson.get("duration").toString()) / (1000 * 60))).append("\n");
        out.append("Total failures count:  ").append(eventJson.get("totalFailedCount")).append("\n");
        out.append("Total passed count:  ").append(eventJson.get("totalPassedCount")).append("\n");
        out.append("Total skipped count:  ").append(eventJson.get("totalSkippedCount")).append("\n");
        out.append("```");
        return out.toString();
    }
}
