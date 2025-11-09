package com.omar.jersey.service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.*;
import io.micrometer.core.instrument.binder.system.*;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;

public class MetricsConfig {

    private static final PrometheusMeterRegistry prometheusRegistry =
            new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);

    static {
        // JVM Metrics
        new ClassLoaderMetrics().bindTo(prometheusRegistry);
        new JvmMemoryMetrics().bindTo(prometheusRegistry);
        new JvmGcMetrics().bindTo(prometheusRegistry);
        new JvmThreadMetrics().bindTo(prometheusRegistry);

        // System Metrics
        new ProcessorMetrics().bindTo(prometheusRegistry);
        new FileDescriptorMetrics().bindTo(prometheusRegistry);
    }

    public static MeterRegistry getRegistry() {
        return prometheusRegistry;
    }
}
