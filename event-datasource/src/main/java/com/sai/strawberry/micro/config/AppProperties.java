package com.sai.strawberry.micro.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * Created by saipkri on 07/09/16.
 */
@Component
@Data
@Configuration
public class AppProperties {
    private String esUrl;
    private int concurrencyFactor;
    private String kafkaBrokersCsv;
    private String mongoHost;
    private int mongoPort;
    private String mongoDb;
}
