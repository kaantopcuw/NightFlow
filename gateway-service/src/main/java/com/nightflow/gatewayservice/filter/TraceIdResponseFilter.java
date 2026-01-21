package com.nightflow.gatewayservice.filter;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class TraceIdResponseFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(TraceIdResponseFilter.class);
    private static final String TRACE_ID_HEADER = "X-Trace-Id";

    private final Tracer tracer;
    private final OpenTelemetry openTelemetry;

    public TraceIdResponseFilter(Tracer tracer, OpenTelemetry openTelemetry) {
        this.tracer = tracer;
        this.openTelemetry = openTelemetry;
        log.info("TraceIdResponseFilter initialized with Manual Propagation (OTel Bridge)");
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        String method = exchange.getRequest().getMethod().name();
        
        // Start Micrometer Span
        Span span = tracer.nextSpan().name(method + " " + path).start();
        TraceContext traceContext = span.context();
        String traceId = traceContext.traceId();
        
        log.info("[{}] {} {}", traceId, method, path);

        // Add to Response header
        exchange.getResponse().beforeCommit(() -> {
            exchange.getResponse().getHeaders().add(TRACE_ID_HEADER, traceId);
            return Mono.empty();
        });

        // Mutate Request Configuration
        ServerHttpRequest.Builder requestBuilder = exchange.getRequest().mutate();
        
        // 1. Add Custom X-Trace-Id Header
        requestBuilder.header(TRACE_ID_HEADER, traceId);

        // 2. Add W3C Trace Context Headers (traceparent) using OpenTelemetry Propagator
        try {
            String spanId = traceContext.spanId();
            Boolean sampled = traceContext.sampled();

            // Create OTel SpanContext from Micrometer TraceContext
            SpanContext otelSpanContext = SpanContext.create(
                    traceId,
                    spanId,
                    Boolean.TRUE.equals(sampled) ? TraceFlags.getSampled() : TraceFlags.getDefault(),
                    TraceState.getDefault()
            );

            // Wrap in OTel Span
            io.opentelemetry.api.trace.Span wrappedSpan = io.opentelemetry.api.trace.Span.wrap(otelSpanContext);

            // Inject into request builder
            try (io.opentelemetry.context.Scope scope = wrappedSpan.makeCurrent()) {
                 openTelemetry.getPropagators().getTextMapPropagator().inject(Context.current(), requestBuilder, (carrier, key, value) -> {
                     carrier.header(key, value);
                 });
            }

        } catch (Exception e) {
            log.error("Failed to inject OTel headers", e);
        }

        ServerHttpRequest mutatedRequest = requestBuilder.build();
        ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();

        return chain.filter(mutatedExchange)
                .doFinally(signalType -> span.end());
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
