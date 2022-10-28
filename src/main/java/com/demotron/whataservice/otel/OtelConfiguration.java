/*
 * Copyright 2002 - 2022 Webb Fontaine Group.
 * All Rights Reserved.
 *
 * All information contained herein is, and remains
 * the property of Webb Fontaine Group and its suppliers,
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to Webb Fontaine Group and its suppliers
 * and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Webb Fontaine Group.
 */
package com.demotron.whataservice.otel;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;

public interface OtelConfiguration {
    String TRACER_NAME = "com.demotron.whataservice";

    static OpenTelemetry initOpenTelemetry() {

        //OpenTelemetry openTelemetry = GlobalOpenTelemetry.get();
        //if (openTelemetry == null) {
            Resource resource = Resource.getDefault()
                .merge(Resource.create(Attributes.of(ResourceAttributes.SERVICE_NAME, "whataservice")));

            SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(BatchSpanProcessor.builder(OtlpGrpcSpanExporter.getDefault()).build())
                .setResource(resource)
                .build();

            OpenTelemetry openTelemetry = OpenTelemetrySdk.builder()
                .setTracerProvider(sdkTracerProvider)
                .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
                .buildAndRegisterGlobal();

            Runtime.getRuntime().addShutdownHook(new Thread(sdkTracerProvider::shutdown));
        //}

        return openTelemetry;
    }
}
