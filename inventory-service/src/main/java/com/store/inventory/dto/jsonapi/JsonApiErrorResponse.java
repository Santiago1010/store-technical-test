package com.store.inventory.dto.jsonapi;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class JsonApiErrorResponse {
    private List<JsonApiError> errors;
}
