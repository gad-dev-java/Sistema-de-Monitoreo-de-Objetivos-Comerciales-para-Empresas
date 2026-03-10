package com.upc.oss.monitoreo.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateStoreRequest(
        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Address is required")
        String address,

        @NotBlank(message = "City is required")
        String city,

        @NotBlank(message = "Company Name is required")
        String companyName
) {
}
