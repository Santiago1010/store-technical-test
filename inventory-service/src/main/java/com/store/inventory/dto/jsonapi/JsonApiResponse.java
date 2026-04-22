package com.store.inventory.dto.jsonapi;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JsonApiResponse {
    private JsonApiData data;
}
