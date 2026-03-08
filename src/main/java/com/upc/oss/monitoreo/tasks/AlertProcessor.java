package com.upc.oss.monitoreo.tasks;

import com.upc.oss.monitoreo.entities.Notification;
import com.upc.oss.monitoreo.entities.Store;
import com.upc.oss.monitoreo.repository.NotificationRepository;
import com.upc.oss.monitoreo.repository.StoreRepository;
import com.upc.oss.monitoreo.service.MonitoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AlertProcessor {
    private final MonitoringService monitoringService;
    private final NotificationRepository notificationRepository;
    private final StoreRepository storeRepository;

    @Scheduled(cron = "0 0 */6 * * *")
    public void processStoreAlerts() {
        List<Store> activesStores = storeRepository.findByStatusTrue();

        for (Store store : activesStores) {
            BigDecimal pc = monitoringService.calculatePerformanceCompliance(store.getIdStore());
            BigDecimal pte = monitoringService.calculateTimeElapsedPercentage(store.getIdStore());

            BigDecimal criticalThreshold = pte.multiply(new BigDecimal("0.7"));

            if (pc.compareTo(criticalThreshold) < 0) {
                createNotification(store, "CRITICAL", "Performance is critically low compared to time elapsed.");
            }
            else if (pc.compareTo(pte) < 0) {
                createNotification(store, "WARNING", "Performance is below the expected time progress.");
            }
        }
    }

    private void createNotification(Store store, String severity, String message) {
        Notification note = Notification.builder()
                .store(store)
                .alertType("ALERTA")
                .severityLevel(severity)
                .message(message)
                .build();
        notificationRepository.save(note);
    }
}
