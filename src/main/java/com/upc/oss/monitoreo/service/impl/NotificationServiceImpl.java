package com.upc.oss.monitoreo.service.impl;

import com.upc.oss.monitoreo.dto.NotificationDto;
import com.upc.oss.monitoreo.entities.Notification;
import com.upc.oss.monitoreo.exception.NotificationNotFoundException;
import com.upc.oss.monitoreo.repository.NotificationRepository;
import com.upc.oss.monitoreo.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;

    @Override
    public List<NotificationDto> getUnreadByStore(Long storeId) {
        return notificationRepository.findByStoreIdStoreAndIsReadFalseOrderByGeneratedAtDesc(storeId)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public void markAsRead(Long notificationId) {
        Notification notificationFound = notificationRepository.findById(notificationId)
                .orElseThrow(()-> new NotificationNotFoundException("Notification not found with id: " + notificationId));

        notificationFound.setIsRead(true);
        notificationRepository.save(notificationFound);
    }

    private NotificationDto mapToDto(Notification notification) {
        return NotificationDto.builder()
                .idNotification(notification.getIdNotification())
                .nameStore(notification.getStore().getName())
                .alertType(notification.getAlertType())
                .severityLevel(notification.getSeverityLevel())
                .message(notification.getMessage())
                .isRead(notification.getIsRead())
                .build();
    }
}
