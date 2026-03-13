package com.upc.oss.monitoreo.tasks;

import com.upc.oss.monitoreo.entities.Notification;
import com.upc.oss.monitoreo.entities.Store;
import com.upc.oss.monitoreo.repository.NotificationRepository;
import com.upc.oss.monitoreo.repository.StoreRepository;
import com.upc.oss.monitoreo.service.MonitoringService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlertProcessor Unit Tests")
class AlertProcessorTest {

    @Mock
    private MonitoringService monitoringService;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private StoreRepository storeRepository;

    @InjectMocks
    private AlertProcessor alertProcessor;

    // ─── Fixtures ─────────────────────────────────────────────────────────────

    private Store storeWithoutRecentAlert;
    private Store storeWithRecentAlert;

    @BeforeEach
    void setUp() {
        // Store que nunca ha generado alerta → debe procesarse siempre
        storeWithoutRecentAlert = Store.builder()
                .idStore(1L)
                .name("Tienda Sin Alerta")
                .status(true)
                .lastAlertGenerated(null)
                .build();

        // Store cuya última alerta fue hace menos de 6 horas → debe saltarse
        storeWithRecentAlert = Store.builder()
                .idStore(2L)
                .name("Tienda Con Alerta Reciente")
                .status(true)
                .lastAlertGenerated(LocalDateTime.now().minusHours(2))
                .build();
    }

    // ─── processStoreAlerts ───────────────────────────────────────────────────

    @Nested
    @DisplayName("processStoreAlerts()")
    class ProcessStoreAlerts {

        // ─── Filtro de cooldown ────────────────────────────────────────────

        @Test
        @DisplayName("Should skip store when last alert was generated less than 6 hours ago")
        void shouldSkipStore_whenLastAlertWasGeneratedLessThan6HoursAgo() {
            // Given
            when(storeRepository.findByStatusTrue()).thenReturn(List.of(storeWithRecentAlert));

            // When
            alertProcessor.processStoreAlerts();

            // Then
            verify(monitoringService, never()).calculatePerformanceCompliance(any());
            verify(monitoringService, never()).calculateTimeElapsedPercentage(any());
            verify(notificationRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should process store when lastAlertGenerated is null")
        void shouldProcessStore_whenLastAlertGeneratedIsNull() {
            // Given
            when(storeRepository.findByStatusTrue()).thenReturn(List.of(storeWithoutRecentAlert));
            when(monitoringService.calculatePerformanceCompliance(1L))
                    .thenReturn(new BigDecimal("50.00"));
            when(monitoringService.calculateTimeElapsedPercentage(1L))
                    .thenReturn(new BigDecimal("50.00"));

            // When
            alertProcessor.processStoreAlerts();

            // Then
            verify(monitoringService, times(1)).calculatePerformanceCompliance(1L);
            verify(monitoringService, times(1)).calculateTimeElapsedPercentage(1L);
        }

        @Test
        @DisplayName("Should process store when last alert was generated more than 6 hours ago")
        void shouldProcessStore_whenLastAlertWasGeneratedMoreThan6HoursAgo() {
            // Given
            Store storeWithOldAlert = Store.builder()
                    .idStore(3L)
                    .name("Tienda Con Alerta Antigua")
                    .status(true)
                    .lastAlertGenerated(LocalDateTime.now().minusHours(7))
                    .build();

            when(storeRepository.findByStatusTrue()).thenReturn(List.of(storeWithOldAlert));
            when(monitoringService.calculatePerformanceCompliance(3L))
                    .thenReturn(new BigDecimal("50.00"));
            when(monitoringService.calculateTimeElapsedPercentage(3L))
                    .thenReturn(new BigDecimal("50.00"));

            // When
            alertProcessor.processStoreAlerts();

            // Then
            verify(monitoringService, times(1)).calculatePerformanceCompliance(3L);
            verify(monitoringService, times(1)).calculateTimeElapsedPercentage(3L);
        }

        // ─── Alerta CRITICAL ──────────────────────────────────────────────

        @Test
        @DisplayName("Should create CRITICAL notification when pc is below 70% of pte")
        void shouldCreateCriticalNotification_whenPcIsBelow70PercentOfPte() {
            // Given
            // pte = 80, criticalThreshold = 80 * 0.7 = 56, pc = 40 < 56 → CRITICAL
            when(storeRepository.findByStatusTrue()).thenReturn(List.of(storeWithoutRecentAlert));
            when(monitoringService.calculatePerformanceCompliance(1L))
                    .thenReturn(new BigDecimal("40.00"));
            when(monitoringService.calculateTimeElapsedPercentage(1L))
                    .thenReturn(new BigDecimal("80.00"));

            // When
            alertProcessor.processStoreAlerts();

            // Then
            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
            verify(notificationRepository, times(1)).save(captor.capture());

            Notification savedNotification = captor.getValue();
            assertEquals("CRITICAL", savedNotification.getSeverityLevel());
            assertEquals("ALERTA", savedNotification.getAlertType());
            assertEquals("Performance is critically low.", savedNotification.getMessage());
            assertFalse(savedNotification.getIsRead());
            assertEquals(storeWithoutRecentAlert, savedNotification.getStore());
        }

        @Test
        @DisplayName("Should update store lastAlertGenerated timestamp when CRITICAL notification is created")
        void shouldUpdateStoreLastAlertGenerated_whenCriticalNotificationIsCreated() {
            // Given
            when(storeRepository.findByStatusTrue()).thenReturn(List.of(storeWithoutRecentAlert));
            when(monitoringService.calculatePerformanceCompliance(1L))
                    .thenReturn(new BigDecimal("40.00"));
            when(monitoringService.calculateTimeElapsedPercentage(1L))
                    .thenReturn(new BigDecimal("80.00"));

            // When
            alertProcessor.processStoreAlerts();

            // Then
            verify(storeRepository, times(1)).save(storeWithoutRecentAlert);
            assertNotNull(storeWithoutRecentAlert.getLastAlertGenerated());
        }

        // ─── Alerta WARNING ───────────────────────────────────────────────

        @Test
        @DisplayName("Should create WARNING notification when pc is below pte but above critical threshold")
        void shouldCreateWarningNotification_whenPcIsBelowPteButAboveCriticalThreshold() {
            // Given
            // pte = 80, criticalThreshold = 80 * 0.7 = 56, pc = 65 → entre 56 y 80 → WARNING
            when(storeRepository.findByStatusTrue()).thenReturn(List.of(storeWithoutRecentAlert));
            when(monitoringService.calculatePerformanceCompliance(1L))
                    .thenReturn(new BigDecimal("65.00"));
            when(monitoringService.calculateTimeElapsedPercentage(1L))
                    .thenReturn(new BigDecimal("80.00"));

            // When
            alertProcessor.processStoreAlerts();

            // Then
            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
            verify(notificationRepository, times(1)).save(captor.capture());

            Notification savedNotification = captor.getValue();
            assertEquals("WARNING", savedNotification.getSeverityLevel());
            assertEquals("ALERTA", savedNotification.getAlertType());
            assertEquals("Performance is below expected progress.", savedNotification.getMessage());
            assertFalse(savedNotification.getIsRead());
        }

        @Test
        @DisplayName("Should update store lastAlertGenerated timestamp when WARNING notification is created")
        void shouldUpdateStoreLastAlertGenerated_whenWarningNotificationIsCreated() {
            // Given
            when(storeRepository.findByStatusTrue()).thenReturn(List.of(storeWithoutRecentAlert));
            when(monitoringService.calculatePerformanceCompliance(1L))
                    .thenReturn(new BigDecimal("65.00"));
            when(monitoringService.calculateTimeElapsedPercentage(1L))
                    .thenReturn(new BigDecimal("80.00"));

            // When
            alertProcessor.processStoreAlerts();

            // Then
            verify(storeRepository, times(1)).save(storeWithoutRecentAlert);
            assertNotNull(storeWithoutRecentAlert.getLastAlertGenerated());
        }

        // ─── Sin alerta ───────────────────────────────────────────────────

        @Test
        @DisplayName("Should not create notification when pc is equal to pte")
        void shouldNotCreateNotification_whenPcIsEqualToPte() {
            // Given
            // pc = pte = 80 → no cumple ninguna condición → sin alerta
            when(storeRepository.findByStatusTrue()).thenReturn(List.of(storeWithoutRecentAlert));
            when(monitoringService.calculatePerformanceCompliance(1L))
                    .thenReturn(new BigDecimal("80.00"));
            when(monitoringService.calculateTimeElapsedPercentage(1L))
                    .thenReturn(new BigDecimal("80.00"));

            // When
            alertProcessor.processStoreAlerts();

            // Then
            verify(notificationRepository, never()).save(any());
            verify(storeRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should not create notification when pc is above pte")
        void shouldNotCreateNotification_whenPcIsAbovePte() {
            // Given
            // pc = 90, pte = 80 → rendimiento por encima del esperado → sin alerta
            when(storeRepository.findByStatusTrue()).thenReturn(List.of(storeWithoutRecentAlert));
            when(monitoringService.calculatePerformanceCompliance(1L))
                    .thenReturn(new BigDecimal("90.00"));
            when(monitoringService.calculateTimeElapsedPercentage(1L))
                    .thenReturn(new BigDecimal("80.00"));

            // When
            alertProcessor.processStoreAlerts();

            // Then
            verify(notificationRepository, never()).save(any());
            verify(storeRepository, never()).save(any());
        }

        // ─── Lista vacía y múltiples stores ──────────────────────────────

        @Test
        @DisplayName("Should not process anything when there are no active stores")
        void shouldNotProcessAnything_whenThereAreNoActiveStores() {
            // Given
            when(storeRepository.findByStatusTrue()).thenReturn(List.of());

            // When
            alertProcessor.processStoreAlerts();

            // Then
            verify(monitoringService, never()).calculatePerformanceCompliance(any());
            verify(monitoringService, never()).calculateTimeElapsedPercentage(any());
            verify(notificationRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should process only stores without recent alerts when mixed list")
        void shouldProcessOnlyStoresWithoutRecentAlerts_whenMixedList() {
            // Given
            when(storeRepository.findByStatusTrue())
                    .thenReturn(List.of(storeWithoutRecentAlert, storeWithRecentAlert));
            when(monitoringService.calculatePerformanceCompliance(1L))
                    .thenReturn(new BigDecimal("50.00"));
            when(monitoringService.calculateTimeElapsedPercentage(1L))
                    .thenReturn(new BigDecimal("50.00"));

            // When
            alertProcessor.processStoreAlerts();

            // Then — solo se procesa storeWithoutRecentAlert (id=1), el otro se saltea
            verify(monitoringService, times(1)).calculatePerformanceCompliance(1L);
            verify(monitoringService, never()).calculatePerformanceCompliance(2L);
        }

        @Test
        @DisplayName("Should create one notification per store that meets alert condition")
        void shouldCreateOneNotificationPerStore_thatMeetsAlertCondition() {
            // Given
            Store secondStore = Store.builder()
                    .idStore(3L)
                    .name("Segunda Tienda")
                    .status(true)
                    .lastAlertGenerated(null)
                    .build();

            when(storeRepository.findByStatusTrue())
                    .thenReturn(List.of(storeWithoutRecentAlert, secondStore));
            when(monitoringService.calculatePerformanceCompliance(1L))
                    .thenReturn(new BigDecimal("40.00"));
            when(monitoringService.calculateTimeElapsedPercentage(1L))
                    .thenReturn(new BigDecimal("80.00"));
            when(monitoringService.calculatePerformanceCompliance(3L))
                    .thenReturn(new BigDecimal("40.00"));
            when(monitoringService.calculateTimeElapsedPercentage(3L))
                    .thenReturn(new BigDecimal("80.00"));

            // When
            alertProcessor.processStoreAlerts();

            // Then — una notificación por cada store que cumple condición
            verify(notificationRepository, times(2)).save(any(Notification.class));
            verify(storeRepository, times(2)).save(any(Store.class));
        }
    }
}