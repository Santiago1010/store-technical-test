package com.store.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final Logger       log     = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final AntPathMatcher MATCHER = new AntPathMatcher();

    private static final List<String[]> PUBLIC_ROUTES = List.of(
        new String[]{"POST",   "/api/v1/auth/register"},
        new String[]{"POST",   "/api/v1/auth/login"},
        new String[]{"GET",    "/api/v1/products/**"},
        new String[]{"GET",    "/actuator/**"},
        new String[]{"GET", "/swagger-ui.html"},
        new String[]{"GET", "/swagger-ui/**"},
        new String[]{"GET", "/v3/api-docs/**"},
        new String[]{"GET", "/webjars/**"}
    );

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public int getOrder() {
        return -100;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest request       = exchange.getRequest();
        String            correlationId = resolveCorrelationId(request);

        ServerHttpRequest mutatedRequest = request.mutate()
            .header("X-Correlation-Id", correlationId)
            .build();
        exchange = exchange.mutate().request(mutatedRequest).build();

        if (isPublicRoute(request)) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("correlationId={} method={} path={} event=missing_token",
                correlationId, request.getMethod(), request.getPath());
            return unauthorized(exchange, correlationId, "Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);

        try {
            Claims claims  = jwtUtil.validateAndExtract(token);
            String username = claims.getSubject();
            String role     = claims.get("role", String.class);

            log.info("correlationId={} username={} role={} path={} event=auth_ok",
                correlationId, username, role, request.getPath());

            ServerHttpRequest authenticatedRequest = exchange.getRequest().mutate()
                .header("X-Auth-User", username)
                .header("X-Auth-Role", role)
                .header("X-Correlation-Id", correlationId)
                .build();

            return chain.filter(exchange.mutate().request(authenticatedRequest).build());

        } catch (JwtException ex) {
            log.warn("correlationId={} path={} event=invalid_token reason={}",
                correlationId, request.getPath(), ex.getMessage());
            return unauthorized(exchange, correlationId, "Invalid or expired token");
        }
    }

    private boolean isPublicRoute(ServerHttpRequest request) {
        String method = request.getMethod() != null
            ? request.getMethod().name() : "";
        String path   = request.getPath().value();

        return PUBLIC_ROUTES.stream().anyMatch(route ->
            route[0].equalsIgnoreCase(method) && MATCHER.match(route[1], path)
        );
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String correlationId, String detail) {
        var response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        response.getHeaders().set("X-Correlation-Id", correlationId);

        String body = """
            {"errors":[{"status":"401","title":"Unauthorized","detail":"%s","meta":{"correlationId":"%s"}}]}
            """.formatted(detail, correlationId).strip();

        var buffer = response.bufferFactory().wrap(body.getBytes());
        return response.writeWith(Mono.just(buffer));
    }

    private String resolveCorrelationId(ServerHttpRequest request) {
        String existing = request.getHeaders().getFirst("X-Correlation-Id");
        return (existing != null && !existing.isBlank()) ? existing : UUID.randomUUID().toString();
    }
}