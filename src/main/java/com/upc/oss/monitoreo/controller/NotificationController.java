package com.upc.oss.monitoreo.controller;

import com.upc.oss.monitoreo.dto.NotificationDto;
import com.upc.oss.monitoreo.dto.response.DataResponse;
import com.upc.oss.monitoreo.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping("/store/{storeId}")
    public ResponseEntity<DataResponse<List<NotificationDto>>> listNotificationsUnreadByStore(@PathVariable Long storeId) {
        List<NotificationDto> notificationDtoList = notificationService.getUnreadByStore(storeId);

        DataResponse<List<NotificationDto>> response = DataResponse.<List<NotificationDto>>builder()
                .status(HttpStatus.OK.value())
                .message("Unread notifications successfully fetching")
                .data(notificationDtoList)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{idNotification}/read")
    public ResponseEntity<Void> readNotification(@PathVariable Long idNotification) {
        notificationService.markAsRead(idNotification);
        return ResponseEntity.ok().build();
    }
}
