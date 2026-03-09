package com.upc.oss.monitoreo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record CompanyDto(
        Long idCompany,
        String name,
        String ruc,
        Boolean status,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate createdAt
) {
}
