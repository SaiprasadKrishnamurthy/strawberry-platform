package com.sai.strawberry.api;

import lombok.Data;

import java.util.List;

/**
 * Created by saipkri on 13/12/16.
 */
@Data
public class CassandraDatabase {
    private List<String> cassandraDDLs;
    private String entityClassName;
}
