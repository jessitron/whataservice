package com.demotron.whataservice;

import java.util.List;
import java.util.function.Function;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;

public class FraudCheckFunctions {

  public static List<Function<String,String>> allChecks = List.of(FraudCheckFunctions::distrustHighInflation, FraudCheckFunctions::distrustSocialism);

  @WithSpan("check for hyperinflation")
	public static String distrustHighInflation(@SpanAttribute("parameter.information") String information) {
		Span span = Span.current();
		span.setAttribute("app.information", information);
		if (information.matches(".*high inflation.*") || information.matches(".*hyperinflation.*") ) {
			return "Oh, that currency is high inflation";
		}
		return null;
	}

  @WithSpan("check for socialism")
	public static String distrustSocialism(@SpanAttribute("parameter.information") String information) {
		Span span = Span.current();
		span.setAttribute("app.information", information);
		if ( information.matches(".*socialism.*") ) {
			return "Oh, that currency can't be trusted";
		}
		return null;
	}
}

