package com.upc.oss.monitoreo.dto.request;

import java.math.BigDecimal;

public record CreateSaleRequest(
        String storeName,
        BigDecimal amount,
        String description
) {
}
