package com.demotron.whataservice;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;

@RestController
public class FraudCheckController {

	@GetMapping(value = "/",
		 produces = "application/json")
	@ResponseBody
	public Map<String,Object> index() {
		var out = new HashMap<String, Object>();
		out.put("message", "thanks for asking. What a service!");
		out.put("sus", false);
		return out;
	}
	


	/* TODO: error handling. Gives a 500 NPE on invalid input 😱 */
	@PostMapping(value = "/",
		 produces = "application/json")
	@ResponseBody 
	public Map<String,Object> index(@RequestBody ChargeRequest input) throws IOException {
		var out = new HashMap<String, Object>();
		out.put("currencyCode", input.amount.currencyCode);
		out.put("sus", specialFraudChecks(input.amount.currencyCode).equals(OK));
		out.put("message", specialFraudChecks(input.amount.currencyCode));
		return out;
	}

	@WithSpan("special fraud check")
	private String specialFraudChecks(String currencyCode) {
    for (int i = 0; i < FraudCheckFunctions.allChecks.size(); i++) 
		{
			var checker = FraudCheckFunctions.allChecks.get(i);
			var result = performOneCheck(currencyCode, checker);
			if (result != null) {
				return result;
			}
		}
		return OK;
	}
	private static final String OK = "Can't complain";

	@WithSpan("one check")
	public String performOneCheck(@SpanAttribute("app.currencyCode") String currencyCode, Function<String, String> checker) {
		Span span = Span.current();
		try {
			Document doc = Jsoup.connect("https://en.wikipedia.org/wiki/ISO_4217").get();
			System.out.println(doc.title());
			span.setAttribute("app.sourceOfInfo", doc.title());
			Elements currencyCodeRows = doc.select("td:containsOwn(" + currencyCode + ")");
			for (Element currencyCodeTd : currencyCodeRows) {
				try {
					String moreInfoUrl = currencyCodeTd.nextElementSibling().nextElementSibling().nextElementSibling().select("a[href]").first().absUrl("href");
					System.out.println(moreInfoUrl);
					Document moreInfoDoc = Jsoup.connect(moreInfoUrl).get();
					String check = checker.apply(moreInfoDoc.body().text());
					if (check != null) {
						return check;
					}
				} catch (Exception e) {
					// well, there might be another one
					span.recordException(e, Attributes.of(AttributeKey.stringKey("currencyCodeTd"), currencyCodeTd.html()));
				}
			}
		} catch(Exception e) {
			span.recordException(e, null);
			e.printStackTrace();
			return "Failed to check";
		}
		return null;
	}
}
