package com.upc.oss.monitoreo.service;

import java.math.BigDecimal;

public interface MonitoringService {
    BigDecimal calculatePerformanceCompliance(Long storeId);
    BigDecimal calculateTimeElapsedPercentage(Long storeId);
}
