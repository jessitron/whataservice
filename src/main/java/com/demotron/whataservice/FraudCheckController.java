package com.demotron.whataservice;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FraudCheckController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FraudCheckController.class);

    private static final String OK = "Can't complain";

    /**
     * mostly for test purposes
     *
     * @return the same thing every time
     */
    @GetMapping(value = "/", produces = "application/json")
    @ResponseBody
    public Map<String, Object> index() {
        var out = new HashMap<String, Object>(2);
        out.put("message", "thanks for asking. What a service!");
        out.put("sus", false);

        return out;
    }

    /* TODO: error handling. Gives a 500 NPE on invalid input 😱 */
    @PostMapping(value = "/", produces = "application/json")
    @ResponseBody
    public Map<String, Object> index(@RequestBody ChargeRequest input) {
        var currencyCode = input.amount.currencyCode;

        var out = new HashMap<String, Object>(3);
        out.put("currencyCode", input.amount.currencyCode);
        out.put("sus", OK.equals(specialFraudChecks(currencyCode)));
        out.put("message", specialFraudChecks(currencyCode));

        return out;
    }

    private static String specialFraudChecks(String currencyCode) {
        if ("USD".equals(currencyCode)) {
            return OK;
        }

        return FraudCheckFunctions.ALL_CHECKS.stream()
            .map(checker -> performOneCheck(currencyCode, checker))
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(OK);
    }

    private static String performOneCheck(String currencyCode, Function<String, String> checker) {
        try {
            Document doc = Jsoup.connect("https://en.wikipedia.org/wiki/ISO_4217").get();
            LOGGER.info("Doc title: {}", doc.title());

            Elements currencyCodeRows = doc.select("td:containsOwn(" + currencyCode + ')');
            for (Element currencyCodeTd : currencyCodeRows) {
                try {
                    String moreInfoUrl = currencyCodeTd.nextElementSibling()
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
