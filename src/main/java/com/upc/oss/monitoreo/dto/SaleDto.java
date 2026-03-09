package com.upc.oss.monitoreo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record SaleDto(
        Long idSale,
        String storeName,
        Boolean storeStatus,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate saleDate,
        BigDecimal amount,
        String description
) {
}
