package com.upc.oss.monitoreo.dto.response;

import java.math.BigDecimal;

public record MonitoringKpiResponse(
        BigDecimal performanceCompliance,
        BigDecimal timeElapsedPercentage
) {
}
