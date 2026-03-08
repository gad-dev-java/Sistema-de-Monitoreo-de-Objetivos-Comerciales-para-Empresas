package com.upc.oss.monitoreo.dto.request;

public record CreateUserRequest(
        String name,
        String email,
        String password,
        String companyName
) {
}
