package com.demotron.whataservice;

import java.util.List;
import java.util.function.Function;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;

public class FraudCheckFunctions {

  public static List<Function<String,String>> allChecks = List.of(FraudCheckFunctions::distrustHighInflation);

  @WithSpan("check for hyperinflation")
	public static String distrustHighInflation(@SpanAttribute("parameter.information") String information) {
		Span span = Span.current();
		span.setAttribute("app.information", information);
		if (information.matches(".*high inflation.*") || information.matches(".*hyperinflation.*") ) {
			return "Oh, that currency is high inflation";
		}
		return null;
	}
}

