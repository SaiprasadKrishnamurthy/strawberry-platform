package com.sai.strawberry.micro;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sai.strawberry.api.EventStreamConfig;

import java.nio.file.Files;
import java.nio.file.Paths;

import static java.util.stream.Collectors.joining;

/**
 * Created by saipkri on 17/11/16.
 */
public class Scratchpad {
    public static void mainz(String[] args) throws Exception {

        /*System.out.println(Strings.commonPrefix("oliviersergent", "oliversargent"));

        System.out.println(StringUtils.getLevenshteinDistance("oliviersergent", "oliversargent"));

        String one = "oliviersergent";
        String two = "oliversargent";

        int longer = one.length() > two.length() ? one.length() : two.length();


        System.out.println(StringUtils.overlay(one, two, 0, longer));


        double[] v1 = {0, 2, 5};
        double[] v2 = {0, 2, 4};
        System.out.println(new EuclideanDistance().compute(v1, v2));

        String vowels = "aeiou";
        int chars = 2;

        recurse(vowels, 0, 2);



    }

    static void recurse(String main, int mainIdx, int n) {

        if (mainIdx >= main.length()) {
            return;
        } else {
            for (int i = mainIdx; i < main.length(); i++) {
                for (int j = i; j < i + n && j < main.length(); j++) {
                    System.out.print(main.toCharArray()[j]);
                }
                System.out.println();
            }
        }
    }*/


        String groovyScript = Files.lines(Paths.get("/Users/saipkri/learning/strawberry/rest/src/main/resources/custom.groovy")).collect(joining("\n"));
        EventStreamConfig doc = new ObjectMapper().readValue(Scratchpad.class.getClassLoader().getResourceAsStream("card_transactions_stream_config.json"), EventStreamConfig.class);
        doc.setCustomProcessingHookScript(groovyScript);

        System.out.println(new ObjectMapper().writeValueAsString(doc));

       /* Binding binding = new Binding();
        binding.setVariable("config", doc);
        binding.setVariable("jsonIn", doc);
        GroovyShell shell = new GroovyShell(binding);
        Map returnDoc = (Map) shell.evaluate(doc.getCustomProcessingHookScript());
        System.out.println(returnDoc);*/


    }

}
