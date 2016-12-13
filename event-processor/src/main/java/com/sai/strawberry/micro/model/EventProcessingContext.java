package com.sai.strawberry.micro.model;

import com.sai.strawberry.api.EventConfig;
import lombok.Data;

import java.util.Map;

/**
 * Created by saipkri on 28/11/16.
 */
@Data
public class EventProcessingContext {
    private final Map doc;
    private final EventConfig config;
    private final long startTimestamp;

    public long timeElapsedSinceStart() {
        return System.currentTimeMillis() - startTimestamp;
    }
}
