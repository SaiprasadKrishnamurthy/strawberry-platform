package com.sai.strawberry.api;

import lombok.Data;

/**
 * Created by saipkri on 12/01/17.
 */
@Data
public class NotificationThrottler {
    private String fieldIdentifier;
    private int numberOfNotifications;
    private int timeWindowInMinutes;
}
