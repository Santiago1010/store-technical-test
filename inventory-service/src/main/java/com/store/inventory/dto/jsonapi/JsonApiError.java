package com.store.inventory.dto.jsonapi;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JsonApiError {
    private String status;
    private String title;
    private String detail;
    private String correlationId;
}
