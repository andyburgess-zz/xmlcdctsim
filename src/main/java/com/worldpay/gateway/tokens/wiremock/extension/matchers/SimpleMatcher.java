package com.worldpay.gateway.tokens.wiremock.extension.matchers;

import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.matching.MatchResult;
import com.github.tomakehurst.wiremock.matching.RegexPattern;

import java.util.Map;

import static com.github.tomakehurst.wiremock.matching.MatchResult.aggregate;
import static com.github.tomakehurst.wiremock.matching.MatchResult.exactMatch;
import static java.util.stream.Collectors.toList;

public class SimpleMatcher {

    private final Request request;
    private final Parameters parameters;

    public SimpleMatcher(Request request, Parameters parameters) {
        this.request = request;
        this.parameters = parameters;
    }

    public MatchResult matchAgainstMethod() {
        return MatchResult.of(
                !parameters.containsKey("method")
                        || parameters.getString("method").equals(request.getMethod().getName())
        );
    }

    public MatchResult matchAgainstUrl() {
        return MatchResult.of(
                !parameters.containsKey("url")
                        || parameters.getString("url").equals(request.getUrl())
        );
    }

    @SuppressWarnings("unchecked")
    public MatchResult matchAgainstHeaders() {
        if (!parameters.containsKey("headers")) {
            return exactMatch();
        }

        return aggregate(
                ((Map<String,String>)parameters.get("headers")).entrySet().stream()
                        .map(kv -> matchAgainstHeader(request.getHeaders().getHeader(kv.getKey()), kv.getValue()))
                        .collect(toList()));
    }

    private MatchResult matchAgainstHeader(HttpHeader header, String value) {
        return MatchResult.of(
                header.isPresent()
                        && header.hasValueMatching(new RegexPattern(value))
        );
    }

}
