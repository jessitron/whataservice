package com.demotron.whataservice;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

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
		out.put("sus", beSerious(input.amount.currencyCode).equals(OK));
		out.put("message", beSerious(input.amount.currencyCode));
		return out;
	}

	private static final String OK = "Can't complain";
	@WithSpan
	public String beSerious(@SpanAttribute("app.currencyCode") String currencyCode) throws IOException {

		Document doc = Jsoup.connect("https://en.wikipedia.org/wiki/ISO_4217").get();
		System.out.println(doc.title());
		Elements currencyCodeRows = doc.select("td:containsOwn(" + currencyCode + ")");
		for (Element currencyCodeTd : currencyCodeRows) {
			String moreInfoUrl = currencyCodeTd.nextElementSibling().nextElementSibling().nextElementSibling().select("a[href]").first().absUrl("href");
			System.out.println(moreInfoUrl);
			Document moreInfoDoc = Jsoup.connect(moreInfoUrl).get();
			String check = distrustHighInflation(moreInfoDoc.body().text());
			if (check != null) {
				return check;
			}
		}
		return OK;
	}

	private String distrustHighInflation(String information) {
     if (information.matches("high inflation") || information.matches("hyperinflation") ) {
			return "Oh, that currency is high inflation";
		 }
		 return null;
	}

}
