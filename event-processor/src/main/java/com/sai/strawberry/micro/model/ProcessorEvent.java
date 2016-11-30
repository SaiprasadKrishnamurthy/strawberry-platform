package com.sai.strawberry.micro.model;

import lombok.Data;

/**
 * Created by saipkri on 30/11/16.
 */
@Data
public class ProcessorEvent {
    private String payloadIdentifier;
    private String[] notifiedTo;
    private String streamId;
    private String originatedTimestamp;
    private long startTime = System.currentTimeMillis();
    private long processingTimeInMillis;
    private boolean error;
}
