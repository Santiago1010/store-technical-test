package com.store.gateway.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AuthResponse(
    @JsonProperty("access_token") String accessToken,
    @JsonProperty("token_type")   String tokenType,
    @JsonProperty("expires_in")   long expiresIn,
    String username,
    String role
) {
    public static AuthResponse of(String token, long expiresInMs, String username, String role) {
        return new AuthResponse(token, "Bearer", expiresInMs / 1000, username, role);
    }
}