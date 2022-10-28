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
package com.demotron.whataservice.core;

import java.util.Objects;
import java.util.function.Function;

import com.demotron.whataservice.otel.OtelConfiguration;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CurrencyService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CurrencyService.class);

    private OpenTelemetry openTelemetry;

    public CurrencyService(OpenTelemetry openTelemetry) {
        this.openTelemetry = openTelemetry;
    }

    public String specialFraudChecks(String currencyCode) {
        if ("USD".equals(currencyCode)) {
            return Response.OK;
        }

        return FraudCheckFunctions.ALL_CHECKS.stream()
            .map(checker -> performOneCheck(currencyCode, checker))
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(Response.OK);
    }

    private String performOneCheck(String currencyCode, Function<String, String> checker) {
        Tracer tracer = openTelemetry.getTracer(OtelConfiguration.TRACER_NAME);
        Span span = tracer.spanBuilder("call wiki for currency").startSpan();

        // Make the span the current span
        try (Scope ss = span.makeCurrent()) {
            return doPerformOneCheck(currencyCode, checker);
        } finally {
            span.end();
        }
    }

    private static String doPerformOneCheck(String currencyCode, Function<String, String> checker) {
        try {
            Document doc = Jsoup.connect("https://en.wikipedia.org/wiki/ISO_4217").get();
            LOGGER.info("Doc title: {}", doc.title());

            Elements currencyCodeRows = doc.select("td:containsOwn(" + currencyCode + ')');
            for (Element currencyCodeTd : currencyCodeRows) {
                try {
                    String moreInfoUrl = currencyCodeTd
                        .nextElementSibling()
                        .nextElementSibling()
                        .nextElementSibling()
                        .select("a[href]")
                        .first()
                        .absUrl("href");

                    LOGGER.info("More info url: {}", moreInfoUrl);

                    Document moreInfoDoc = Jsoup.connect(moreInfoUrl).get();
                    String check = checker.apply(moreInfoDoc.body().text());
                    if (check != null) {
                        return check;
                    }
                } catch (Exception e) {
                    LOGGER.trace("Cannot parse", e);
                    // well, there might be another currencyCodeRow that works, so keep going
                }
            }
        } catch (Exception e) {
            LOGGER.error("", e);
            return "Failed to check";
        }
        return null;
    }
}
