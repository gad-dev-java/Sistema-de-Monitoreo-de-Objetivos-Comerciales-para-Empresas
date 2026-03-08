package com.upc.oss.monitoreo.service;

import com.upc.oss.monitoreo.dto.NotificationDto;

import java.util.List;

public interface NotificationService {
    List<NotificationDto> getUnreadByStore(Long storeId);
    void markAsRead(Long notificationId);
}
