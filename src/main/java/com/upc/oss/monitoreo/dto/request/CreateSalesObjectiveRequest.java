package com.upc.oss.monitoreo.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateSalesObjectiveRequest(
        String nameStore,
        BigDecimal targetAmount,
        String periodType,
        LocalDate startDate,
        LocalDate endDate
) {
}
