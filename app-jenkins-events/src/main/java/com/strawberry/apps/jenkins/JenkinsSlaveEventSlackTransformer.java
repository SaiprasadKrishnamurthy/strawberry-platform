package com.strawberry.apps.jenkins;

import com.sai.strawberry.api.Callback;

import java.util.Map;

/**
 * Created by saipkri on 04/01/17.
 */
public class JenkinsSlaveEventSlackTransformer implements Callback {
    @Override
    public String call(final Map eventJson) {
        StringBuilder out = new StringBuilder();
        out.append("* Jenkins Slave ").append(eventJson.get("displayName")).append(" is Offline!*\n");
        out.append("```");
        out.append("Disk Space : ").append(eventJson.get("diskSpace")).append("\n");
        out.append("OS Type :  ").append(eventJson.get("osType")).append("\n");
        out.append("Offline Reason :  ").append(eventJson.get("offlineReason")).append("\n");
        out.append("```");
        return out.toString();
    }
}
