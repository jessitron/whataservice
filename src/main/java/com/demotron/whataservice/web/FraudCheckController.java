package com.demotron.whataservice.web;

import java.util.HashMap;
import java.util.Map;

import com.demotron.whataservice.core.CurrencyService;
import com.demotron.whataservice.core.Response;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FraudCheckController {

    private CurrencyService currencyService;

    public FraudCheckController(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

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
        out.put("sus", Response.OK.equals(currencyService.specialFraudChecks(currencyCode)));
        out.put("message", currencyService.specialFraudChecks(currencyCode));

        return out;
    }

}
