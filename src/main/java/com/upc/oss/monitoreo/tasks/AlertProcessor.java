package com.upc.oss.monitoreo.tasks;

import com.upc.oss.monitoreo.entities.Notification;
import com.upc.oss.monitoreo.entities.Store;
import com.upc.oss.monitoreo.repository.NotificationRepository;
import com.upc.oss.monitoreo.repository.StoreRepository;
import com.upc.oss.monitoreo.service.MonitoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlertProcessor {
    private final MonitoringService monitoringService;
    private final NotificationRepository notificationRepository;
    private final StoreRepository storeRepository;

    @Scheduled(fixedRate = 21_600_000)
    public void processStoreAlerts() {
        log.info("Iniciando procesamiento de alertas - {}", LocalDateTime.now());
        List<Store> activesStores = storeRepository.findByStatusTrue();
        log.info("Locales activos encontrados: {}", activesStores.size());
        LocalDateTime now = LocalDateTime.now();

        for (Store store : activesStores) {
            if (store.getLastAlertGenerated() != null &&
                    ChronoUnit.SECONDS.between(store.getLastAlertGenerated(), now) < 21_600) {
                continue;
            }

            BigDecimal pc = monitoringService.calculatePerformanceCompliance(store.getIdStore());
            BigDecimal pte = monitoringService.calculateTimeElapsedPercentage(store.getIdStore());
            BigDecimal criticalThreshold = pte.multiply(new BigDecimal("0.7"));

            if (pc.compareTo(criticalThreshold) < 0) {
                createNotification(store, "CRITICAL", "Performance is critically low.");
                updateStoreAlertTimestamp(store, now);
            } else if (pc.compareTo(pte) < 0) {
                createNotification(store, "WARNING", "Performance is below expected progress.");
                updateStoreAlertTimestamp(store, now);
            }
        }
    }

    private void updateStoreAlertTimestamp(Store store, LocalDateTime now) {
        store.setLastAlertGenerated(now);
        storeRepository.save(store);
    }

    private void createNotification(Store store, String severity, String message) {
        Notification note = Notification.builder()
                .store(store)
                .alertType("ALERTA")
                .severityLevel(severity)
                .isRead(false)
                .message(message)
                .build();
        notificationRepository.save(note);
    }
}
