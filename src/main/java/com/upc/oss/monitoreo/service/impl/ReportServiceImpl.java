package com.upc.oss.monitoreo.service.impl;

import com.upc.oss.monitoreo.entities.Store;
import com.upc.oss.monitoreo.repository.StoreRepository;
import com.upc.oss.monitoreo.service.MonitoringService;
import com.upc.oss.monitoreo.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {
    private final MonitoringService monitoringService;
    private final StoreRepository storeRepository;

    @Override
    public String generateComplianceCsv() {
        StringBuilder csv = new StringBuilder();
        csv.append("Store Name,PC (%),PT (%),Status\n");

        List<Store> stores =storeRepository.findByStatusTrue();
        for (Store store : stores) {
            BigDecimal pc = monitoringService.calculatePerformanceCompliance(store.getIdStore());
            BigDecimal pt = monitoringService.calculateTimeElapsedPercentage(store.getIdStore());
            String status = (pc.compareTo(pt) >= 0) ? "ON TRACK" : "AT RISK";

            csv.append(String.format("%s,%.2f,%.2f,%s\n",
                    store.getName(), pc, pt, status));
        }

        return csv.toString();
    }
}
