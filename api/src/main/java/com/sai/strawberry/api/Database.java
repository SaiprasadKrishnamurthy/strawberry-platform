package com.sai.strawberry.api;

import lombok.Data;

/**
 * Created by saipkri on 13/12/16.
 */
@Data
public class Database {
    private CassandraDatabase cassandra;
    private MongoDatabase mongo;

}
