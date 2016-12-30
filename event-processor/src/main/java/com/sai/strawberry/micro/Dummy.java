//package com.sai.strawberry.micro;
//
//import com.google.common.reflect.ClassPath;
//import com.sai.strawberry.micro.eventlistener.EventListener;
//import org.apache.commons.lang3.ClassPathUtils;
//import org.apache.log4j.Logger;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
//import org.springframework.boot.builder.SpringApplicationBuilder;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.io.ResourceLoader;
//import org.springframework.kafka.listener.MessageListener;
//import org.springframework.util.ResourceUtils;
//
//import static java.util.stream.Collectors.joining;
//
///**
// * Created by saipkri on 07/09/16.
// */
//@Configuration
//@EnableAutoConfiguration
//public class Dummy {
//
//    private static final Logger LOGGER = Logger.getLogger(Dummy.class);
//
//    @Autowired
//    private ResourceLoader resourceLoader;
//
//    public static void main(String[] args) throws Exception {
//        SpringApplicationBuilder application = new SpringApplicationBuilder();
//        ApplicationContext ctx = application //
//                .headless(true) //
//                .addCommandLineProperties(true) //
//                .sources(Dummy.class) //
//                .main(Dummy.class) //
//                .registerShutdownHook(true)
//                .run(args);
//    }
//}
