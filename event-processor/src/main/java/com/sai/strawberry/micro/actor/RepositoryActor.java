package com.sai.strawberry.micro.actor;

import akka.actor.UntypedActor;
import com.sai.strawberry.api.EventStreamConfig;
import com.sai.strawberry.micro.config.ActorFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

/**
 * Created by saipkri on 08/09/16.
 */
public class RepositoryActor extends UntypedActor {

    public static long timeout_in_seconds = 5 * 1000;

    private final MongoTemplate mongoTemplate;
    private final ActorFactory actorFactory;

    public RepositoryActor(final MongoTemplate mongoTemplate, final ActorFactory actorFactory) {
        this.mongoTemplate = mongoTemplate;
        this.actorFactory = actorFactory;
    }


    @Override
    public void onReceive(final Object message) throws Throwable {
        if (message instanceof EventStreamConfig) {
            EventStreamConfig config = (EventStreamConfig) message;
            Query query = new Query();
            query.addCriteria(Criteria.where("configId").is(config.getConfigId()));
            mongoTemplate.remove(query, EventStreamConfig.class);
            mongoTemplate.save(message);
        } else if (message instanceof Class) {
            getSender().tell(mongoTemplate.findAll((Class<Object>) message), getSelf());
        } else if(message instanceof String) {
            Query query = new Query();
            query.addCriteria(Criteria.where("configId").is(message.toString()));
            getSender().tell(mongoTemplate.findOne(query, EventStreamConfig.class), getSelf());
        }
    }
}
