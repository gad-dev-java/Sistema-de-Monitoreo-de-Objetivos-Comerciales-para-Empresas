package com.upc.oss.monitoreo.dto.request;

public record AuthRequest(
        String email,
        String password
) {
}
