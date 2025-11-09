package com.omar.jersey.service;

import io.micrometer.prometheus.PrometheusMeterRegistry;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/metrics")
public class MetricsResource {

    private final PrometheusMeterRegistry registry =
            (PrometheusMeterRegistry) MetricsConfig.getRegistry();

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String scrape() {
        return registry.scrape();
    }
}
