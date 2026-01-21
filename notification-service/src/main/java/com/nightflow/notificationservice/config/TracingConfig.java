package com.nightflow.notificationservice.config;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.otel.bridge.OtelCurrentTraceContext;
import io.micrometer.tracing.otel.bridge.OtelPropagator;
import io.micrometer.tracing.otel.bridge.OtelTracer;
import io.micrometer.tracing.propagation.Propagator;
import io.micrometer.tracing.handler.DefaultTracingObservationHandler;
import io.micrometer.tracing.handler.PropagatingReceiverTracingObservationHandler;
import io.micrometer.tracing.handler.PropagatingSenderTracingObservationHandler;
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
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.filter.ServerHttpObservationFilter;

import java.time.Duration;

@Configuration
public class TracingConfig {

    private static final Logger log = LoggerFactory.getLogger(TracingConfig.class);

    @Value("${spring.application.name:notification-service}")
    private String serviceName;

    @Bean
    @Primary
    public OpenTelemetry openTelemetry() {
        log.info("FORCED TRACING CONFIG (FULL MANUAL) for service: {}", serviceName);
        Resource resource = Resource.getDefault()
                .merge(Resource.create(Attributes.of(
                        AttributeKey.stringKey("service.name"), serviceName)));

        OtlpGrpcSpanExporter spanExporter = OtlpGrpcSpanExporter.builder()
                .setEndpoint("http://localhost:4317")
                .setTimeout(Duration.ofSeconds(10))
                .build();

        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
                .setResource(resource)
                .addSpanProcessor(SimpleSpanProcessor.create(spanExporter))
                .build();

        OpenTelemetrySdk openTelemetry = OpenTelemetrySdk.builder()
                .setTracerProvider(tracerProvider)
                .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
                .build();

        Runtime.getRuntime().addShutdownHook(new Thread(tracerProvider::close));
        return openTelemetry;
    }

    @Bean
    @Primary
    public Tracer micrometerTracer(OpenTelemetry openTelemetry) {
        io.opentelemetry.api.trace.Tracer otelTracer = openTelemetry.getTracer(serviceName);
        OtelCurrentTraceContext currentTraceContext = new OtelCurrentTraceContext();
        return new OtelTracer(otelTracer, currentTraceContext, event -> {});
    }

    @Bean
    @Primary
    public Propagator propagator(OpenTelemetry openTelemetry) {
        return new OtelPropagator(openTelemetry.getPropagators(), openTelemetry.getTracer(serviceName));
    }

    @Bean
    public DefaultTracingObservationHandler defaultTracingObservationHandler(Tracer tracer) {
        return new DefaultTracingObservationHandler(tracer);
    }

    @Bean
    public PropagatingReceiverTracingObservationHandler<io.micrometer.observation.transport.ReceiverContext> 
    propagatingReceiverTracingObservationHandler(Tracer tracer, Propagator propagator) {
        return new PropagatingReceiverTracingObservationHandler<>(tracer, propagator);
    }

    @Bean
    public PropagatingSenderTracingObservationHandler<io.micrometer.observation.transport.SenderContext> 
    propagatingSenderTracingObservationHandler(Tracer tracer, Propagator propagator) {
        return new PropagatingSenderTracingObservationHandler<>(tracer, propagator);
    }

    @Bean
    @ConditionalOnMissingBean
    public FilterRegistrationBean<ServerHttpObservationFilter> observationFilter(ObservationRegistry registry) {
        FilterRegistrationBean<ServerHttpObservationFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new ServerHttpObservationFilter(registry));
        registrationBean.setOrder(org.springframework.core.Ordered.HIGHEST_PRECEDENCE); 
        return registrationBean;
    }
}
