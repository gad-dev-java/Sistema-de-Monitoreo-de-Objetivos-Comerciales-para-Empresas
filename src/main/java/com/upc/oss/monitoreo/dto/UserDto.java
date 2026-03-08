package com.upc.oss.monitoreo.dto;

import lombok.Builder;

@Builder
public record UserDto(
        Long idUser,
        String name,
        String email,
        String role,
        String companyName,
        Boolean companyStatus
) {
}
