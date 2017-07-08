package com.worldpay.gateway.tokens.wiremock;

import com.github.tomakehurst.wiremock.standalone.CommandLineOptions;
import com.github.tomakehurst.wiremock.standalone.WireMockServerRunner;

public class WpgWireMockServerRunner {

    private static CommandLineOptions options = new CommandLineOptions();

    public static void main(String... args) {
        options = new CommandLineOptions(args);
        WireMockServerRunner.main(args);
    }

    public static CommandLineOptions getOptions() {
        return options;
    }
}
