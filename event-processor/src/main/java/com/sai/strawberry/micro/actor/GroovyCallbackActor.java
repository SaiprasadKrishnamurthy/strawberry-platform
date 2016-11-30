package com.sai.strawberry.micro.actor;

import akka.actor.UntypedActor;
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
public class GroovyCallbackActor extends UntypedActor {

    public static long timeout_in_seconds = 5 * 1000;

    private final MongoTemplate mongoTemplate;
    private final MongoTemplate mongoTemplateBatch;

    public GroovyCallbackActor(final MongoTemplate mongoTemplate, final MongoTemplate mongoTemplateBatch) {
        this.mongoTemplate = mongoTemplate;
        this.mongoTemplateBatch = mongoTemplateBatch;
    }


    @Override
    public void onReceive(final Object message) throws Throwable {
        Object _ctx = message;
        if (message instanceof EventProcessingContext) {
            EventProcessingContext context = (EventProcessingContext) message;
            if (context.getConfig().getCustomProcessingHookScript() != null) {
                _ctx = new EventProcessingContext(invokeScript(context.getConfig(), context.getDoc()), context.getConfig(), context.getStartTimestamp());
            }
        }
        getSender().tell(_ctx, getSelf());
    }

    private Map invokeScript(final EventStreamConfig eventStreamConfig, final Map jsonIn) throws Exception {
        Binding binding = new Binding();
        binding.setVariable("config", eventStreamConfig);
        binding.setVariable("jsonIn", jsonIn);
        binding.setVariable("slowZoneMongoTemplate", mongoTemplate);
        binding.setVariable("fastZoneMongoTemplate", mongoTemplateBatch);
        GroovyShell shell = new GroovyShell(binding);
        Object returnDoc = shell.evaluate(eventStreamConfig.getCustomProcessingHookScript());
        Map jsonCopy = new LinkedHashMap<>(jsonIn);
        jsonCopy.put("custom__", returnDoc);
        return jsonCopy;
    }
}
