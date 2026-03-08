package com.upc.oss.monitoreo.dto.request;

public record UpdateStoreRequest(
        String name,
        String address,
        String city,
        String companyName
) {
}
