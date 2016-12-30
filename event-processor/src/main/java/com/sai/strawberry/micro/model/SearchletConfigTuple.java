package com.sai.strawberry.micro.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by saipkri on 30/12/16.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchletConfigTuple {
    private String eventConfigId;
    private Object searchCriteria;
}
