package com.store.gateway.common.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Component
@Order(-1)
public class GlobalErrorHandler implements ErrorWebExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalErrorHandler.class);

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {

        String correlationId = exchange.getRequest()
            .getHeaders().getFirst("X-Correlation-Id");
        if (correlationId == null) correlationId = "unknown";

        HttpStatus status;
        String      title;
        String      detail;

        if (ex instanceof WebExchangeBindException bindEx) {
            status = HttpStatus.UNPROCESSABLE_ENTITY;
            title  = "Validation Error";
            detail = bindEx.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));

        } else if (ex instanceof ResponseStatusException rsEx) {
            status = HttpStatus.resolve(rsEx.getStatusCode().value());
            if (status == null) status = HttpStatus.INTERNAL_SERVER_ERROR;
            title  = status.getReasonPhrase();
            detail = rsEx.getReason() != null ? rsEx.getReason() : ex.getMessage();

        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            title  = "Internal Server Error";
            detail = "An unexpected error occurred";
            log.error("correlationId={} event=unhandled_error", correlationId, ex);
        }

        log.warn("correlationId={} status={} title={} detail={}",
            correlationId, status.value(), title, detail);

        String body = """
            {"errors":[{"status":"%d","title":"%s","detail":"%s","meta":{"correlationId":"%s"}}]}
            """.formatted(status.value(), title, detail, correlationId).strip();

        var response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        response.getHeaders().set("X-Correlation-Id", correlationId);

        var buffer = response.bufferFactory().wrap(body.getBytes());
        return response.writeWith(Mono.just(buffer));
    }
}