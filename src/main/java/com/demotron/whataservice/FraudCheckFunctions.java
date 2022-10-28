package com.demotron.whataservice;

import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;

public enum FraudCheckFunctions {
    ;

    private static final Pattern HIGH_INF_PATTERN = Pattern.compile(".*high inflation.*");
    private static final Pattern HYPER_INF_PATTERN = Pattern.compile(".*hyperinflation.*");
    private static final Pattern SOCIALISM_PATTERN = Pattern.compile(".*socialism.*");
    private static final Pattern HIGH_VOL_PATTERN = Pattern.compile(".*high volatility.*");
    private static final Pattern EXTREME_VOL_PATTERN = Pattern.compile(".*extreme volatility.*");

    public static final List<Function<String, String>> ALL_CHECKS = List.of(
        FraudCheckFunctions::distrustVolatility,
        FraudCheckFunctions::distrustHighInflation,
        FraudCheckFunctions::distrustSocialism
    );

    public static String distrustHighInflation(String information) {
        if (HIGH_INF_PATTERN.matcher(information).matches() || HYPER_INF_PATTERN.matcher(information).matches()) {
            return "Oh, that currency is high inflation";
        }
        return null;
    }

    public static String distrustSocialism(String information) {
        if (SOCIALISM_PATTERN.matcher(information).matches()) {
            return "Oh, that currency can't be trusted";
        }
        return null;
    }

    public static String distrustVolatility(String information) {
        if (HIGH_VOL_PATTERN.matcher(information).matches() || EXTREME_VOL_PATTERN.matcher(information).matches()) {
            return "Oh, that currency is too volatile";
        }
        return null;
    }
}

