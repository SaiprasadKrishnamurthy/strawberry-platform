package com.sai.strawberry.micro.actor;

import akka.actor.UntypedActor;
import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import com.sai.strawberry.api.CassandraBackedDataTransformer;
import com.sai.strawberry.api.CustomProcessorHook;
import com.sai.strawberry.api.EventConfig;
import com.sai.strawberry.micro.model.EventProcessingContext;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.List;
import java.util.Map;

/**
 * Created by saipkri on 08/09/16.
 */
public class AppCallbackActor extends UntypedActor {

    public static long timeout_in_seconds = 5 * 1000;

    private final MongoTemplate mongoTemplate;
    private final MongoTemplate mongoTemplateBatch;
    private final Session cassandraSession;


    public AppCallbackActor(final MongoTemplate mongoTemplate, final MongoTemplate mongoTemplateBatch, final Session cassandraSession) {
        this.mongoTemplate = mongoTemplate;
        this.mongoTemplateBatch = mongoTemplateBatch;
        this.cassandraSession = cassandraSession;
    }


    @Override
    public void onReceive(final Object message) throws Throwable {
        Object _ctx = message;
        if (message instanceof EventProcessingContext) {
            EventProcessingContext context = (EventProcessingContext) message;
            if (context.getConfig().getDataTransformation() != null
                    && context.getConfig().getDataTransformation().getDataTransformerHookClass() != null) {
                Map doc = invokeCallback(context.getConfig(), context.getDoc());

                // Add the important metadata back in the new transformed doc.
                doc.put("__configId__", context.getDoc().get("__configId__"));
                doc.put("__naturalId__", context.getDoc().get("__naturalId__"));

                _ctx = new EventProcessingContext(doc, context.getConfig(), context.getStartTimestamp());
            }
        }
        getSender().tell(_ctx, getSelf());
    }

    private Map invokeCallback(final EventConfig eventStreamConfig, final Map jsonIn) throws Exception {
        Class<?> aClass = Class.forName(eventStreamConfig.getDataTransformation().getDataTransformerHookClass());
        if (CustomProcessorHook.class.isAssignableFrom(aClass)) {
            Class<CustomProcessorHook> callback = (Class<CustomProcessorHook>) aClass;
            return callback.newInstance().execute(eventStreamConfig, jsonIn, mongoTemplate, mongoTemplateBatch);
        } else {
            MappingManager mappingManager = new MappingManager(cassandraSession);
            CassandraBackedDataTransformer cassandraBackedDataTransformer = (CassandraBackedDataTransformer) aClass.newInstance();
            List<Object> entitiesToBeSaved = cassandraBackedDataTransformer.entities(cassandraSession, mappingManager, eventStreamConfig, jsonIn);
            entitiesToBeSaved.forEach(entityToBeSaved -> {
                Mapper<Object> mapper = mappingManager.mapper((Class<Object>) entityToBeSaved.getClass());
                mapper.save(entityToBeSaved);
            });
            return cassandraBackedDataTransformer.process(cassandraSession, mappingManager, eventStreamConfig, jsonIn);
        }
    }
}