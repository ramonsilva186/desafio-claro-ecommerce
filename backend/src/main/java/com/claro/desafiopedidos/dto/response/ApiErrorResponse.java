package com.claro.desafiopedidos.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

public record ApiErrorResponse(
        LocalDateTime timestamp,
        int Status,
        String error,
        String message,
        String path,
        Map<String,String> fieldErrors
) {
}
