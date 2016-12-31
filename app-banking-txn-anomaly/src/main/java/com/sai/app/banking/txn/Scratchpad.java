package com.sai.app.banking.txn;

import com.datastax.driver.core.*;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import org.apache.commons.codec.binary.Base64;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

/**
 * Created by saipkri on 14/12/16.
 */
public class Scratchpad {
    public static void main(String[] args) throws Exception {


        PoolingOptions poolingOptions = new PoolingOptions();
        poolingOptions.setMaxRequestsPerConnection(HostDistance.LOCAL , 30000);
// customize options...

        Cluster cluster = Cluster.builder()
                .addContactPoint("127.0.0.1")
                .withPoolingOptions(poolingOptions)
                .withSocketOptions(
                        new SocketOptions()
                                .setConnectTimeoutMillis(5000))
//                .withCredentials("cassandra", "cassandra")
                .build();
        System.out.println(cluster.getMetadata().getAllHosts());
        Runnable r = () -> {
            Session session = cluster.connect("sai_1");
            String cql = "CREATE TABLE if not exists personsss (id uuid PRIMARY KEY, name text, nationality text, gender text)";
            session.execute(cql);

            System.out.println("Done...");
            session.close();
            cluster.closeAsync();
        };
        new Thread(r).start();
        Thread.sleep(10000);


    }
}
