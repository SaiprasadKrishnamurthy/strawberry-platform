package com.sai.strawberry.micro.model;

import com.sai.strawberry.api.EventStreamConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Created by saipkri on 28/11/16.
 */
@Data
public class EventProcessingContext {
    private final Map doc;
    private final EventStreamConfig config;
    private final long startTimestamp;

    public long timeElapsedSinceStart() {
        return System.currentTimeMillis() - startTimestamp;
    }
}
