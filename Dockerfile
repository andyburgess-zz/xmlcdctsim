FROM rodolpheche/wiremock

COPY stubs /home/wiremock

ADD target/xml-cdct-sim-jar-with-dependencies.jar /var/wiremock/extensions/

CMD ["java", "-cp", "/var/wiremock/lib/*:/var/wiremock/extensions/*", \
    "com.worldpay.gateway.tokens.wiremock.WpgWireMockServerRunner", \
    "--extensions", \
    "com.worldpay.gateway.tokens.wiremock.extension.WpgResponseTemplateTransformer,com.worldpay.gateway.tokens.wiremock.extension.WpgRequestMatcher", \
    "--no-request-journal" \
    ]
