package com.demotron.whataservice;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

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
	


	@PostMapping(value = "/",
		 produces = "application/json")
	@ResponseBody
	public Map<String,Object> index(@RequestBody ChargeRequest input) {
		var out = new HashMap<String, Object>();
		out.put("sus", false);
		return out;
	}
}
