package com.hims.utils;

import com.hims.constants.NotificationType;

import java.util.Map;

public record NotificationEvent(
        NotificationType type,
        String mobileNumber,
        Map<String, String> variables

) {
}