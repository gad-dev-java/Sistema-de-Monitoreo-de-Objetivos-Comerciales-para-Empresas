package com.upc.oss.monitoreo.service.impl;

import com.upc.oss.monitoreo.dto.NotificationDto;
import com.upc.oss.monitoreo.entities.Notification;
import com.upc.oss.monitoreo.entities.Store;
import com.upc.oss.monitoreo.exception.NotificationNotFoundException;
import com.upc.oss.monitoreo.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private Notification notification1;
    private Notification notification2;

    @BeforeEach
    void setUp() {

        Store store = Store.builder()
                .idStore(1L)
                .name("Tienda Central")
                .build();

        notification1 = Notification.builder()
                .idNotification(1L)
                .store(store)
                .alertType("VENTAS BAJAS")
                .severityLevel("ALTA")
                .message("Las ventas están por debajo del objetivo")
                .isRead(false)
                .generatedAt(LocalDate.from(LocalDateTime.now()))
                .build();

        notification2 = Notification.builder()
                .idNotification(2L)
                .store(store)
                .alertType("VENTAS BAJAS")
                .severityLevel("MEDIA")
                .message("Rendimiento bajo en el periodo")
                .isRead(false)
                .generatedAt(LocalDate.from(LocalDateTime.now()))
                .build();
    }

    @Test
    @DisplayName("Debe retornar lista de notificaciones no leídas")
    void getUnreadByStore_DebeRetornarListaDto() {

        List<Notification> notifications = Arrays.asList(notification1, notification2);

        when(notificationRepository
                .findByStoreIdStoreAndIsReadFalseOrderByGeneratedAtDesc(1L))
                .thenReturn(notifications);

        List<NotificationDto> result = notificationService.getUnreadByStore(1L);

        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals("Tienda Central", result.getFirst().nameStore());
        assertEquals("Las ventas están por debajo del objetivo", result.getFirst().message());

        verify(notificationRepository, times(1))
                .findByStoreIdStoreAndIsReadFalseOrderByGeneratedAtDesc(1L);
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay notificaciones")
    void getUnreadByStore_ListaVacia() {

        when(notificationRepository
                .findByStoreIdStoreAndIsReadFalseOrderByGeneratedAtDesc(1L))
                .thenReturn(List.of());

        List<NotificationDto> result = notificationService.getUnreadByStore(1L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Debe marcar notificación como leída")
    void markAsRead_DebeActualizarEstado() {

        when(notificationRepository.findById(1L))
                .thenReturn(Optional.of(notification1));

        notificationService.markAsRead(1L);

        assertTrue(notification1.getIsRead());

        verify(notificationRepository, times(1)).findById(1L);
        verify(notificationRepository, times(1)).save(notification1);
    }

    @Test
    @DisplayName("Debe lanzar excepción si notificación no existe")
    void markAsRead_NotificacionNoExiste() {

        when(notificationRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(NotificationNotFoundException.class, () -> notificationService.markAsRead(99L));

        verify(notificationRepository, times(1)).findById(99L);
        verify(notificationRepository, never()).save(any());
    }
}