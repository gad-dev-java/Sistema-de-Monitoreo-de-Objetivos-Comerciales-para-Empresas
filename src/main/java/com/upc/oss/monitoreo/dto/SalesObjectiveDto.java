package com.upc.oss.monitoreo.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record SalesObjectiveDto(
        Long idObjective,
        String nameStore,
        Boolean statusStore,
        BigDecimal targetAmount,
        String periodType,
        LocalDate startDate,
        LocalDate endDate
) {
}
