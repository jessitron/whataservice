package com.demotron.whataservice;

import java.util.List;
import java.util.function.Function;

public class FraudCheckFunctions {

  public static List<Function<String,String>> allChecks = List.of(
    FraudCheckFunctions::distrustVolatility, 
    FraudCheckFunctions::distrustHighInflation, 
    FraudCheckFunctions::distrustSocialism);

	public static String distrustHighInflation( String information) {
		if (information.matches(".*high inflation.*") || information.matches(".*hyperinflation.*") ) {
			return "Oh, that currency is high inflation";
		}
		return null;
	}

	public static String distrustSocialism( String information) {
		if ( information.matches(".*socialism.*") ) {
			return "Oh, that currency can't be trusted";
		}
		return null;
	}

	public static String distrustVolatility(String information) {
		if ( information.matches(".*high volatility.*") || information.matches(".*extreme volatility.*")) {
			return "Oh, that currency is too volatile";
		}
		return null;
	}
}

