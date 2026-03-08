package com.upc.oss.monitoreo.dto.request;

public record CreateStoreRequest(
        String name,
        String address,
        String city,
        String companyName
) {
}
