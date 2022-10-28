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
		var span = Span.current();
		var currencyCode = input.amount.currencyCode;
		span.setAttribute("app.currencyCode", currencyCode);
		var out = new HashMap<String, Object>();
		out.put("currencyCode",input.amount.currencyCode);
		out.put("sus", specialFraudChecks(currencyCode).equals(OK));
		out.put("message", specialFraudChecks(currencyCode));
		span.setAttribute("app.result.sus", out.get("sus").toString());
		span.setAttribute("app.result.message", out.get("message").toString());
		return out;
	}

	@WithSpan("special fraud check")
	private String specialFraudChecks(String currencyCode) {
		if (currencyCode.equals("USD")) {
			return OK;
		}
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
	public String performOneCheck(String currencyCode, Function<String, String> checker) {
		Span span = Span.current();
		span.setAttribute("app.currencyCode", currencyCode);
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
					// well, there might be another currencyCodeRow that works, so keep going
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
