package com.upc.oss.monitoreo.controller;

import com.upc.oss.monitoreo.dto.NotificationDto;
import com.upc.oss.monitoreo.exception.GlobalExceptionHandler;
import com.upc.oss.monitoreo.exception.NotificationNotFoundException;
import com.upc.oss.monitoreo.exception.StoreNotFoundException;
import com.upc.oss.monitoreo.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationController Unit Tests")
class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;

    private MockMvc mockMvc;

    private NotificationDto notificationDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(notificationController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        notificationDto = NotificationDto.builder()
                .idNotification(1L)
                .nameStore("Mi Tienda")
                .alertType("SALES_BELOW_TARGET")
                .severityLevel("HIGH")
                .message("Sales are below the target for this period")
                .isRead(false)
                .build();
    }

    @Nested
    @DisplayName("GET /api/notifications/store/{storeId}")
    class ListNotificationsUnreadByStore {

        @Test
        @DisplayName("Should return 200 OK with unread notifications when store has unread notifications")
        void shouldReturn200_withUnreadNotifications_whenStoreHasUnreadNotifications() throws Exception {
            // Given
            when(notificationService.getUnreadByStore(1L)).thenReturn(List.of(notificationDto));

            // When / Then
            mockMvc.perform(get("/api/notifications/store/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("Unread notifications successfully fetching"))
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].idNotification").value(1L))
                    .andExpect(jsonPath("$.data[0].nameStore").value("Mi Tienda"))
                    .andExpect(jsonPath("$.data[0].alertType").value("SALES_BELOW_TARGET"))
                    .andExpect(jsonPath("$.data[0].severityLevel").value("HIGH"))
                    .andExpect(jsonPath("$.data[0].message").value("Sales are below the target for this period"))
                    .andExpect(jsonPath("$.data[0].isRead").value(false));
        }

        @Test
        @DisplayName("Should return 200 OK with empty list when store has no unread notifications")
        void shouldReturn200_withEmptyList_whenStoreHasNoUnreadNotifications() throws Exception {
            // Given
            when(notificationService.getUnreadByStore(1L)).thenReturn(List.of());

            // When / Then
            mockMvc.perform(get("/api/notifications/store/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data", hasSize(0)));
        }

        @Test
        @DisplayName("Should return 200 OK with timestamp in response")
        void shouldReturn200_withTimestampInResponse() throws Exception {
            // Given
            when(notificationService.getUnreadByStore(1L)).thenReturn(List.of(notificationDto));

            // When / Then
            mockMvc.perform(get("/api/notifications/store/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.timestamp").isNotEmpty());
        }

        @Test
        @DisplayName("Should return only notifications with isRead false")
        void shouldReturnOnlyUnreadNotifications_withIsReadFalse() throws Exception {
            // Given
            NotificationDto anotherUnread = NotificationDto.builder()
                    .idNotification(2L)
                    .nameStore("Mi Tienda")
                    .alertType("TIME_ELAPSED")
                    .severityLevel("MEDIUM")
                    .message("75% of the period has elapsed")
                    .isRead(false)
                    .build();
            when(notificationService.getUnreadByStore(1L)).thenReturn(List.of(notificationDto, anotherUnread));

            // When / Then
            mockMvc.perform(get("/api/notifications/store/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(2)))
                    .andExpect(jsonPath("$.data[0].isRead").value(false))
                    .andExpect(jsonPath("$.data[1].isRead").value(false));
        }

        @Test
        @DisplayName("Should return 404 NOT FOUND when store does not exist")
        void shouldReturn404_whenStoreDoesNotExist() throws Exception {
            // Given
            when(notificationService.getUnreadByStore(99L))
                    .thenThrow(new StoreNotFoundException("Store not found with id 99"));

            // When / Then
            mockMvc.perform(get("/api/notifications/store/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.message").value("Store not found with id 99"));
        }

        @Test
        @DisplayName("Should call service with correct storeId from path variable")
        void shouldCallService_withCorrectStoreIdFromPathVariable() throws Exception {
            // Given
            when(notificationService.getUnreadByStore(3L)).thenReturn(List.of());

            // When
            mockMvc.perform(get("/api/notifications/store/3"))
                    .andExpect(status().isOk());

            // Then
            verify(notificationService, times(1)).getUnreadByStore(3L);
        }
    }

    @Nested
    @DisplayName("PUT /api/notifications/{idNotification}/read")
    class ReadNotification {

        @Test
        @DisplayName("Should return 200 OK with no body when notification is marked as read")
        void shouldReturn200_withNoBody_whenNotificationIsMarkedAsRead() throws Exception {
            // Given
            doNothing().when(notificationService).markAsRead(1L);

            // When / Then
            mockMvc.perform(put("/api/notifications/1/read"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(""));
        }

        @Test
        @DisplayName("Should call service with correct idNotification from path variable")
        void shouldCallService_withCorrectIdNotificationFromPathVariable() throws Exception {
            // Given
            doNothing().when(notificationService).markAsRead(5L);

            // When
            mockMvc.perform(put("/api/notifications/5/read"))
                    .andExpect(status().isOk());

            // Then
            verify(notificationService, times(1)).markAsRead(5L);
        }

        @Test
        @DisplayName("Should return 404 NOT FOUND when notification does not exist")
        void shouldReturn404_whenNotificationDoesNotExist() throws Exception {
            // Given
            doThrow(new NotificationNotFoundException("Notification not found with id 99"))
                    .when(notificationService).markAsRead(99L);

            // When / Then
            mockMvc.perform(put("/api/notifications/99/read"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.message").value("Notification not found with id 99"));
        }

        @Test
        @DisplayName("Should call service exactly once when marking notification as read")
        void shouldCallServiceExactlyOnce_whenMarkingNotificationAsRead() throws Exception {
            // Given
            doNothing().when(notificationService).markAsRead(1L);

            // When
            mockMvc.perform(put("/api/notifications/1/read"))
                    .andExpect(status().isOk());

            // Then
            verify(notificationService, times(1)).markAsRead(1L);
            verifyNoMoreInteractions(notificationService);
        }
    }
}