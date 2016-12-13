package com.sai.strawberry.api;

import lombok.Data;

/**
 * Created by saipkri on 13/12/16.
 */
@Data
public class MongoDatabase {
    private long maxNumberOfDocsBatch;
    private long maxBatchSizeInBytes;

}
