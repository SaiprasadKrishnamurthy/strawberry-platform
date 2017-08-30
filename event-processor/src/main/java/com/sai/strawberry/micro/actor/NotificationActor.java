package com.sai.strawberry.micro.actor;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.MappingManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sai.strawberry.api.ConditionEvaluatorParamsHolder;
import com.sai.strawberry.api.EventConfig;
import com.sai.strawberry.api.Handler;
import com.sai.strawberry.api.NotificationThrottler;
import com.sai.strawberry.micro.config.ActorFactory;
import com.sai.strawberry.micro.model.NotificationTuple;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by saipkri on 08/09/16.
 */
public class NotificationActor extends UntypedActor {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final ActorFactory actorFactory;
    private final MongoTemplate mongoTemplate;
    private final MongoTemplate mongoBatchTemplate;
    private final Session cassandraSession;
    private final MappingManager cassandraMappingManager;


    public static long timeout_in_seconds = 5 * 1000;

    private final KafkaProducer<String, String> sender;


    public NotificationActor(final KafkaProducer<String, String> sender, final ActorFactory actorFactory, final MongoTemplate mongoTemplate, final MongoTemplate mongoBatchTemplate, final Session cassandraSession, final MappingManager cassandraMappingManager) {
        this.actorFactory = actorFactory;
        this.sender = sender;
        this.mongoTemplate = mongoTemplate;
        this.mongoBatchTemplate = mongoBatchTemplate;
        this.cassandraSession = cassandraSession;
        this.cassandraMappingManager = cassandraMappingManager;
    }

    @Override
    public void onReceive(final Object message) throws Throwable {
        if (message instanceof NotificationTuple) {

            NotificationTuple notificationTuple = (NotificationTuple) message;

            boolean shouldNotify = true;
            // Throttle notifications if needed.
            if (notificationTuple.getNotificationConfig().getThrottle() != null) {
                NotificationThrottler throttler = notificationTuple.getNotificationConfig().getThrottle();
                String fieldName = throttler.getFieldIdentifier().trim();
                String fieldValue = notificationTuple.getContext().getDoc().get(throttler.getFieldIdentifier().trim()).toString();
                int noOfNotifications = throttler.getNumberOfNotifications();
                int timeWindowMinutes = throttler.getTimeWindowInMinutes();

                // query mongo.
                Query query = new Query(Criteria.where(fieldName)
                        .is(fieldValue)
                        .andOperator(Criteria.where("event").is(notificationTuple.getContext().getConfig().getConfigId())));

                query.with(new Sort(Sort.Direction.DESC, "notifiedTimestamp")).limit(noOfNotifications);

                List<Map> recentlyNotifiedEvents = mongoTemplate.find(query, Map.class, "notification_throttles");

                shouldNotify = recentlyNotifiedEvents.size() < noOfNotifications;
                if (!shouldNotify && recentlyNotifiedEvents != null && !recentlyNotifiedEvents.isEmpty()) {
                    // find the difference between n, n-1, n-2 etc.
                    long current = System.currentTimeMillis();
                    long window = timeWindowMinutes * 60 * 1000;
                    int count = 0;

                    for (Map event : recentlyNotifiedEvents) {
                        long time = (long) event.get("notifiedTimestamp");
                        if ((current - time) < window) {
                            count++;
                        }
                    }
                    shouldNotify = count < noOfNotifications;
                }
                if (shouldNotify) {
                    Map<String, Object> notificationTimestamps = new HashMap<>();
                    notificationTimestamps.put(fieldName, fieldValue);
                    notificationTimestamps.put("notifiedTimestamp", System.currentTimeMillis());
                    notificationTimestamps.put("event", notificationTuple.getContext().getConfig().getConfigId());
                    mongoTemplate.save(notificationTimestamps, "notification_throttles");
                }
            }

            if (shouldNotify && notificationTuple.getContext().shouldNotifyToKafkaTopic(notificationTuple.getNotificationChannel())) {
                sender.send(new ProducerRecord<>(notificationTuple.getNotificationChannel(), MAPPER.writeValueAsString(notificationTuple.getContext().getDoc())));
            }

            // Call the handler if any.
            if (StringUtils.isNotBlank(notificationTuple.getNotificationConfig().getNotificationHandlerClass())) {
                invokeHandler(notificationTuple.getNotificationConfig().getNotificationHandlerClass().trim(), notificationTuple.getContext().getDoc(), notificationTuple.getContext().getConfig());
            }

            if (shouldNotify) {
                // Additionally publish to any webhooks.
                ActorRef webhooksActor = actorFactory.newActor(WebhooksNotificationActor.class);
                webhooksActor.tell(message, webhooksActor);
            }
        }
    }

    private void invokeHandler(final String className, final Map jsonIn, final EventConfig eventConfig) throws Exception {
        Class<Handler> handler = (Class<Handler>) Class.forName(className);
        handler.newInstance().handle(new ConditionEvaluatorParamsHolder(mongoTemplate, mongoBatchTemplate, cassandraSession, cassandraMappingManager, eventConfig, jsonIn));
    }
}
