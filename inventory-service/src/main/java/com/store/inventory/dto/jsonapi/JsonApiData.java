package com.store.inventory.dto.jsonapi;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class JsonApiData {
    private String id;
    private String type;
    private Map<String, Object> attributes;
}
