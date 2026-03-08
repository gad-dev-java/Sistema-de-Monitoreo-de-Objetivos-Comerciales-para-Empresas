package com.upc.oss.monitoreo.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record DataResponse<T>(
        int status,
        String message,
        T data,
        LocalDateTime timestamp
) {
}
