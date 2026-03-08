package com.upc.oss.monitoreo.dto;

import lombok.Builder;

@Builder
public record StoreDto(
        Long idStore,
        String name,
        String address,
        String city,
        String companyName,
        String companyRuc,
        Boolean companyStatus
) {
}
