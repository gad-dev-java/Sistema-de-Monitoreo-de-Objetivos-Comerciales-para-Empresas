package com.upc.oss.monitoreo.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank(message = "Name is required")
        String name,

        @Email(message = "Invalid email format")
        @NotBlank(message = "Email is required")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be longer than 6 characters")
        String password,

        @NotBlank(message = "Role is required")
        String role,

        @NotBlank(message = "Company Name is required")
        String companyName
) {
}
