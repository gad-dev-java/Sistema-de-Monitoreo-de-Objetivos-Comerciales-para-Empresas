package com.upc.oss.monitoreo.dto;

import lombok.Builder;

@Builder
public record NotificationDto(
        Long idNotification,
        String nameStore,
        String alertType,
        String severityLevel,
        String message,
        Boolean isRead
) {
}
