package com.upc.oss.monitoreo.controller;

import com.upc.oss.monitoreo.dto.response.DataResponse;
import com.upc.oss.monitoreo.dto.response.MonitoringKpiResponse;
import com.upc.oss.monitoreo.service.MonitoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/monitoring")
public class MonitoringController {
    private final MonitoringService monitoringService;

    @GetMapping("/kpi/{storeId}")
    public ResponseEntity<DataResponse<MonitoringKpiResponse>> getStoreKpis(@PathVariable Long storeId) {
        BigDecimal pc = monitoringService.calculatePerformanceCompliance(storeId);
        BigDecimal pt = monitoringService.calculateTimeElapsedPercentage(storeId);

        MonitoringKpiResponse kpiData = new MonitoringKpiResponse(pc, pt);

        DataResponse<MonitoringKpiResponse> response = DataResponse.<MonitoringKpiResponse>builder()
                .status(HttpStatus.OK.value())
                .message("KPIs calculated successfully for store: " + storeId)
                .data(kpiData)
                .timestamp(java.time.LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }
}
