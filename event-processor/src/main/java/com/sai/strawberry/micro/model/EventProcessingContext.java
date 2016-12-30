package com.sai.strawberry.micro.model;

import com.sai.strawberry.api.EventConfig;
import com.sai.strawberry.api.NotificationConfig;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by saipkri on 28/11/16.
 */
@Data
public class EventProcessingContext {
    private final Map doc;
    private final EventConfig config;
    private final long startTimestamp;

    public boolean shouldNotifyToKafkaTopic(final String channelName) {
        NotificationConfig notificationConfig = null;
        if (config.getNotification().getElasticsearch() != null) {
            notificationConfig = notificationConfig(channelName, config.getNotification().getElasticsearch().getNotificationConfigs());
        }
        if (config.getNotification().getSpel() != null) {
            notificationConfig = notificationConfig(channelName, config.getNotification().getSpel().getNotificationConfigs());
        }
        if (config.getNotification().getSql() != null) {
            notificationConfig = notificationConfig(channelName, config.getNotification().getSql().getNotificationConfigs());
        }
        return notificationConfig != null && notificationConfig.isDurable();
    }

    private NotificationConfig notificationConfig(final String channelName, final List<NotificationConfig> notificationConfigs) {
        if (notificationConfigs != null) {
            Optional<NotificationConfig> config = notificationConfigs.stream()
                    .filter(notificationConfig -> notificationConfig.getChannelName().equals(channelName))
                    .findFirst();
            return config.isPresent() ? config.get() : null;
        } else {
            return null;
        }
    }

    public long timeElapsedSinceStart() {
        return System.currentTimeMillis() - startTimestamp;
    }
}
