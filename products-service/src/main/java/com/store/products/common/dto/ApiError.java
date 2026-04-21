package com.store.products.common.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ApiError {
    private List<ErrorObject> errors;

    @Data
    @Builder
    public static class ErrorObject {
        private String status;
        private String title;
        private String detail;
        private String correlationId;
    }
}
