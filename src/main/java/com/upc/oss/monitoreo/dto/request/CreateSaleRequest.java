package com.upc.oss.monitoreo.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreateSaleRequest(
        @NotBlank(message = "Store Name is required")
        String storeName,

        @DecimalMin(value = "0.01", message = "Minimum is 0.01")
        @DecimalMax(value = "99999999.99", message = "Maximum limit exceeded")
        @Digits(integer = 10, fraction = 2, message = "Invalid number format")
        BigDecimal amount,

        @NotBlank(message = "Description is required")
        String description
) {
}
