package com.nightflow.gatewayservice.config;

import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.otel.bridge.OtelCurrentTraceContext;
import io.micrometer.tracing.otel.bridge.OtelTracer;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class TracingConfig {

    private static final Logger log = LoggerFactory.getLogger(TracingConfig.class);

    @Value("${spring.application.name:gateway-service}")
    private String serviceName;

    @Bean
    @ConditionalOnMissingBean
    public OpenTelemetry openTelemetry() {
        String grpcEndpoint = "http://localhost:4317";
        log.info("Configuring OpenTelemetry with gRPC endpoint: {} and service: {}", grpcEndpoint, serviceName);
        
        // Resource with service name
        Resource resource = Resource.getDefault()
                .merge(Resource.create(Attributes.of(
                        AttributeKey.stringKey("service.name"), serviceName)));

        // OTLP gRPC exporter with timeout and insecure connection
        OtlpGrpcSpanExporter spanExporter = OtlpGrpcSpanExporter.builder()
                .setEndpoint(grpcEndpoint)
                .setTimeout(Duration.ofSeconds(10))
                .build();

        // Tracer provider with SimpleSpanProcessor for immediate export
        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
                .setResource(resource)
                .addSpanProcessor(SimpleSpanProcessor.create(spanExporter))
                .build();

        // Build OpenTelemetry with W3C propagators
        OpenTelemetrySdk openTelemetry = OpenTelemetrySdk.builder()
                .setTracerProvider(tracerProvider)
                .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
                .build();

        // Register shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down OpenTelemetry...");
            tracerProvider.close();
        }));

        log.info("OpenTelemetry configured successfully with gRPC exporter");
        return openTelemetry;
    }

    @Bean
    @ConditionalOnMissingBean
    public Tracer micrometerTracer(OpenTelemetry openTelemetry) {
        io.opentelemetry.api.trace.Tracer otelTracer = openTelemetry.getTracer(serviceName);
        OtelCurrentTraceContext currentTraceContext = new OtelCurrentTraceContext();
        log.info("Micrometer Tracer created for service: {}", serviceName);
        return new OtelTracer(otelTracer, currentTraceContext, event -> {
            log.debug("Span event: {}", event);
        });
    }
}
