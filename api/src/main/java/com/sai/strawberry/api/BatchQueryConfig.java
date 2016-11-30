package com.sai.strawberry.api;

import lombok.Data;

/**
 * Created by saipkri on 11/11/16.
 */
@Data
public class BatchQueryConfig {
    private int maxNumberOfDocs;
    private int maxBatchSizeInBytes;
}
