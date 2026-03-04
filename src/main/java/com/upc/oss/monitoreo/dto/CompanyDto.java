package com.upc.oss.monitoreo.dto;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record CompanyDto(
        Long idCompany,
        String name,
        String ruc,
        Boolean status,
        LocalDate createdAt
) {
}
