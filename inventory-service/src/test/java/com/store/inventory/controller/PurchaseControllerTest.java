package com.store.inventory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.store.inventory.dto.PurchaseRequest;
import com.store.inventory.dto.jsonapi.JsonApiData;
import com.store.inventory.dto.jsonapi.JsonApiResponse;
import com.store.inventory.exception.InsufficientStockException;
import com.store.inventory.service.InventoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PurchaseController.class)
class PurchaseControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean  InventoryService inventoryService;

    @Test
    void purchase_returnsOk() throws Exception {
        var response = JsonApiResponse.builder()
            .data(JsonApiData.builder().id(UUID.randomUUID().toString())
                .type("inventory").attributes(Map.of("available", 7)).build())
            .build();
        when(inventoryService.purchase(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/purchases")
                .header("Idempotency-Key", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new PurchaseRequest(UUID.randomUUID(), 3))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.type").value("inventory"));
    }

    @Test
    void purchase_insufficientStock_returns422() throws Exception {
        when(inventoryService.purchase(any(), any()))
            .thenThrow(new InsufficientStockException(5, 2));

        mockMvc.perform(post("/api/v1/purchases")
                .header("Idempotency-Key", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new PurchaseRequest(UUID.randomUUID(), 5))))
            .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void purchase_missingIdempotencyKey_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/purchases")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new PurchaseRequest(UUID.randomUUID(), 1))))
            .andExpect(status().isBadRequest());
    }
}