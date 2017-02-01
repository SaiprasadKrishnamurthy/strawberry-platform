package com.sai.strawberry.api;

import lombok.Data;

/**
 * Created by saipkri on 01/02/17.
 */
@Data
public class Neo4JDatabase {
    private String dbUrl;
    private String dbUsername;
    private String dbPassword;
    private String javaEntityPackages;
}
