package com.sai.app.banking.txn;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.offbytwo.jenkins.JenkinsServer;
import com.sai.strawberry.api.EventConfig;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.HostnameVerifier;
import java.io.File;
import java.net.URI;
import java.util.Map;

/**
 * Created by saipkri on 14/12/16.
 */
public class Scratchpad {
    public static void main(String[] args) throws Exception {

        /*JenkinsServer jenkins = new JenkinsServer(new URI("http://cvg-jenkinsci-sjc4.cisco.com/jenkins/"), "saiprkri", "Saiprasad4$");
        System.out.println(jenkins);
        System.out.println(jenkins.getComputers());*/

//        new ObjectMapper().readValue(new File("/Users/saipkri/learning/new/strawberry/app-banking-txn-anomaly/src/main/resources/card_transactions_stream_config_1.json"), EventConfig.class);


        SSLUtil.turnOffSslChecking();
        RestTemplate restTemplate = new RestTemplate();
        String plainCreds = "saiprkri:e533819fe260fd34a1afb458019863b2";
        byte[] plainCredsBytes = plainCreds.getBytes();
        byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
        String base64Creds = new String(base64CredsBytes);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Basic " + base64Creds);

        HttpEntity<String> request = new HttpEntity<String>(headers);
        ResponseEntity<String> response = restTemplate.exchange("https://cvg-jenkinsci-sjc4.cisco.com/jenkins/computer/api/xml", HttpMethod.GET, request, String.class);
        System.out.println(response.getBody());


    }
}
