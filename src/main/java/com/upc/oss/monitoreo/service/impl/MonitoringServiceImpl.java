package com.upc.oss.monitoreo.service.impl;

import com.upc.oss.monitoreo.entities.SalesObjective;
import com.upc.oss.monitoreo.enums.SalesObjectiveStatus;
import com.upc.oss.monitoreo.exception.ObjectiveStoreActiveNotFound;
import com.upc.oss.monitoreo.repository.SaleRepository;
import com.upc.oss.monitoreo.repository.SalesObjectiveRepository;
import com.upc.oss.monitoreo.service.MonitoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class MonitoringServiceImpl implements MonitoringService {
    private final SaleRepository saleRepository;
    private final SalesObjectiveRepository salesObjectiveRepository;

    @Override
    public BigDecimal calculatePerformanceCompliance(Long storeId) {
        SalesObjective objective = salesObjectiveRepository.findActiveObjectiveByStoreId(storeId, SalesObjectiveStatus.ACTIVO)
                .orElseThrow(() -> new ObjectiveStoreActiveNotFound("No active objective found for this store: " + storeId));

        BigDecimal totalSales = saleRepository.sumSalesByStoreAndPeriod(storeId, objective.getStartDate(), LocalDate.now());

        if (totalSales == null) totalSales = BigDecimal.ZERO;

        if (objective.getTargetAmount().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return totalSales.divide(objective.getTargetAmount(), 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calculateTimeElapsedPercentage(Long storeId) {
        SalesObjective objective = salesObjectiveRepository.findActiveObjectiveByStoreId(storeId, SalesObjectiveStatus.ACTIVO)
                .orElseThrow(() -> new ObjectiveStoreActiveNotFound("No active objective found for this store: " + storeId));

        long totalDays = ChronoUnit.DAYS.between(objective.getStartDate(), objective.getEndDate());
        long elapsedDays = ChronoUnit.DAYS.between(objective.getStartDate(), LocalDate.now());

        if (totalDays <= 0 || elapsedDays < 0) return BigDecimal.ZERO;

        BigDecimal elapsed = BigDecimal.valueOf(elapsedDays);
        BigDecimal total = BigDecimal.valueOf(totalDays);
        BigDecimal hundred = new BigDecimal("100");

        BigDecimal tep = elapsed.divide(total, 4, RoundingMode.HALF_UP)
                .multiply(hundred)
                .setScale(2, RoundingMode.HALF_UP);

        return tep.compareTo(hundred) > 0 ? hundred : tep;
    }
}
