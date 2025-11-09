package com.omar.jersey.service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

@Provider
public class MetricsRequestFilter implements ContainerResponseFilter {

    private final MeterRegistry registry;

    public MetricsRequestFilter() {
        this.registry = MetricsConfig.getRegistry();
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        String path = requestContext.getUriInfo().getPath();
        String method = requestContext.getMethod();
        int status = responseContext.getStatus();

        Timer.builder("http_server_requests_seconds")
                .description("Duration of HTTP server requests")
                .tag("uri", path)
                .tag("method", method)
                .tag("status", String.valueOf(status))
                .register(registry)
                .record(() -> {
                    // placeholder: actual timing instrumentation could be added here
                });
    }
}
