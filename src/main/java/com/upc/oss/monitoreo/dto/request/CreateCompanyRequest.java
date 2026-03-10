package com.upc.oss.monitoreo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateCompanyRequest(
        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Ruc is required")
        @Pattern(regexp = "^[0-9]{11}$", message = "The RUC must have exactly 11 numbers")
        String ruc
) {
}
