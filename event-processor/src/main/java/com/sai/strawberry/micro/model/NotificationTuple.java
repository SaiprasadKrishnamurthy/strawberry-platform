package com.sai.strawberry.micro.model;

import com.sai.strawberry.api.NotificationConfig;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by saipkri on 28/11/16.
 */
@Data
@AllArgsConstructor
public class NotificationTuple {
    private EventProcessingContext context;
    private String notificationChannel;
    private NotificationConfig notificationConfig;
}
