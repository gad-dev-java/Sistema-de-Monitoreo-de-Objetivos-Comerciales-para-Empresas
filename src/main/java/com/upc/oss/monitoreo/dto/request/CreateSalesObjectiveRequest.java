package com.upc.oss.monitoreo.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateSalesObjectiveRequest(
        @NotBlank(message = "Store Name is required")
        String nameStore,

        @DecimalMin(value = "0.01", message = "Minimum is 0.01")
        @DecimalMax(value = "99999999.99", message = "Maximum limit exceeded")
        @Digits(integer = 10, fraction = 2, message = "Invalid number format")
        BigDecimal targetAmount,

        @NotBlank(message = "Period Type is required")
        String periodType,

        @NotNull(message = "Start Date is required")
        @FutureOrPresent(message = "Start Date must be present or future")
        LocalDate startDate,

        @Future(message = "End Date must be in the future")
        @NotNull(message = "End Date is required")
        LocalDate endDate
) {
}
