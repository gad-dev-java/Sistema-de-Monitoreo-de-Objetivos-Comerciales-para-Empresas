package com.upc.oss.monitoreo.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record SaleDto(
        Long idSale,
        String storeName,
        Boolean storeStatus,
        LocalDate saleDate,
        BigDecimal amount,
        String description
) {
}
