package com.sai.strawberry.micro.actor;

import akka.actor.UntypedActor;
import com.sai.strawberry.api.CustomProcessorHook;
import com.sai.strawberry.api.EventStreamConfig;
import com.sai.strawberry.micro.model.EventProcessingContext;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by saipkri on 08/09/16.
 */
public class AppCallbackActor extends UntypedActor {

    public static long timeout_in_seconds = 5 * 1000;

    private final MongoTemplate mongoTemplate;
    private final MongoTemplate mongoTemplateBatch;

    public AppCallbackActor(final MongoTemplate mongoTemplate, final MongoTemplate mongoTemplateBatch) {
        this.mongoTemplate = mongoTemplate;
        this.mongoTemplateBatch = mongoTemplateBatch;
    }


    @Override
    public void onReceive(final Object message) throws Throwable {
        Object _ctx = message;
        if (message instanceof EventProcessingContext) {
            EventProcessingContext context = (EventProcessingContext) message;
            if (context.getConfig().getCustomProcessingHookClassName() != null) {
                System.out.println(" _______________________________ ");
                Map doc = invokeCallback(context.getConfig(), context.getDoc());
                System.out.println(doc);
                System.out.println(" _______________________________ ");
                _ctx = new EventProcessingContext(doc, context.getConfig(), context.getStartTimestamp());
            }
        }
        getSender().tell(_ctx, getSelf());
    }

    private Map invokeCallback(final EventStreamConfig eventStreamConfig, final Map jsonIn) throws Exception {
        Class<CustomProcessorHook> callback = (Class<CustomProcessorHook>) Class.forName(eventStreamConfig.getCustomProcessingHookClassName());
        return callback.newInstance().execute(eventStreamConfig, jsonIn, mongoTemplate, mongoTemplateBatch);
    }
}
