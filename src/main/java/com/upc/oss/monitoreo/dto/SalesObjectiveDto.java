package com.upc.oss.monitoreo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate startDate,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate endDate
) {
}
