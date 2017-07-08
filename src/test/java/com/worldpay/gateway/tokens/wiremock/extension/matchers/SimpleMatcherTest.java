package com.worldpay.gateway.tokens.wiremock.extension.matchers;

import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.MatchResult;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.worldpay.gateway.tokens.wiremock.testsupport.MockRequest.mockRequest;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SimpleMatcherTest {

    @Test
    public void matchAgainstMethod_returnsExactMatch() {
        SimpleMatcher matcher = new SimpleMatcher(
                mockRequest().method(RequestMethod.POST),
                Parameters.one("method", "POST")
        );

        MatchResult actual = matcher.matchAgainstMethod();

        assertTrue(actual.isExactMatch());
    }

    @Test
    public void matchAgainstMethod_returnsNoMatch() {
        SimpleMatcher matcher = new SimpleMatcher(
                mockRequest().method(RequestMethod.POST),
                Parameters.one("method", "GET")
        );

        MatchResult actual = matcher.matchAgainstMethod();

        assertFalse(actual.isExactMatch());
    }

    @Test
    public void matchAgainstMethod_returnsExactMatchWhenParameterNotPresent() {
        SimpleMatcher matcher = new SimpleMatcher(
                mockRequest(),
                Parameters.empty()
        );

        MatchResult actual = matcher.matchAgainstMethod();

        assertTrue(actual.isExactMatch());
    }

    @Test
    public void matchAgainstUrl_returnsExactMatch() {
        SimpleMatcher matcher = new SimpleMatcher(
                mockRequest().url("/a/test/url.html"),
                Parameters.one("url", "/a/test/url.html")
        );

        MatchResult actual = matcher.matchAgainstUrl();

        assertTrue(actual.isExactMatch());
    }

    @Test
    public void matchAgainstUrl_returnsNoMatch() {
        SimpleMatcher matcher = new SimpleMatcher(
                mockRequest().url("/a/test/url.html"),
                Parameters.one("url", "/a/different/test/url.html")
        );

        MatchResult actual = matcher.matchAgainstUrl();

        assertFalse(actual.isExactMatch());
    }

    @Test
    public void matchAgainstUrl_returnsExactMatchWhenParameterNotPresent() {
        SimpleMatcher matcher = new SimpleMatcher(
                mockRequest(),
                Parameters.empty()
        );

        MatchResult actual = matcher.matchAgainstUrl();

        assertTrue(actual.isExactMatch());
    }

    @Test
    public void matchAgainstHeaders_singleHeaderReturnsExactMatch() {
        Map<String, String> headerParams = new HashMap<>();
        headerParams.put("X-WP-CorrelationId", "TEST_CORRELATION");

        SimpleMatcher matcher = new SimpleMatcher(
                mockRequest().withHeader("X-WP-CorrelationId", "TEST_CORRELATION"),
                Parameters.one("headers", headerParams)
        );

        MatchResult actual = matcher.matchAgainstHeaders();

        assertTrue(actual.isExactMatch());
    }

    @Test
    public void matchAgainstHeaders_singleHeaderReturnsNoMatch() {
        Map<String, String> headerParams = new HashMap<>();
        headerParams.put("X-WP-CorrelationId", "OTHER_HEADER");

        SimpleMatcher matcher = new SimpleMatcher(
                mockRequest().withHeader("X-WP-CorrelationId", "TEST_CORRELATION"),
                Parameters.one("headers", headerParams)
        );

        MatchResult actual = matcher.matchAgainstHeaders();

        assertFalse(actual.isExactMatch());
    }

    @Test
    public void matchAgainstHeaders_singleHeaderReturnsExactMatchWhenParameterNotPresent() {
        SimpleMatcher matcher = new SimpleMatcher(
                mockRequest(),
                Parameters.empty()
        );

        MatchResult actual = matcher.matchAgainstHeaders();

        assertTrue(actual.isExactMatch());
    }

    @Test
    public void matchAgainstHeaders_singleHeaderReturnsNoMatchWhenHeaderNotPresent() {
        Map<String, String> headerParams = new HashMap<>();
        headerParams.put("X-WP-CorrelationId", "TEST_CORRELATION");

        SimpleMatcher matcher = new SimpleMatcher(
                mockRequest(),
                Parameters.one("headers", headerParams)
        );

        MatchResult actual = matcher.matchAgainstHeaders();

        assertFalse(actual.isExactMatch());
    }

    @Test
    public void matchAgainstHeaders_multiHeaderReturnsExactMatch() {
        Map<String, String> headerParams = new HashMap<>();
        headerParams.put("SomeHeader", "A value");
        headerParams.put("AnotherHeader", "A different value");

        SimpleMatcher matcher = new SimpleMatcher(
                mockRequest()
                        .withHeader("SomeHeader", "A value")
                        .withHeader("AnotherHeader", "A different value"),
                Parameters.one("headers", headerParams)
        );

        MatchResult actual = matcher.matchAgainstHeaders();

        assertTrue(actual.isExactMatch());
    }

    @Test
    public void matchAgainstHeaders_multiHeaderReturnsNoMatchForSingleWrongHeader() {
        Map<String, String> headerParams = new HashMap<>();
        headerParams.put("SomeHeader", "A wrong value");
        headerParams.put("AnotherHeader", "A different value");

        SimpleMatcher matcher = new SimpleMatcher(
                mockRequest()
                        .withHeader("SomeHeader", "A value")
                        .withHeader("AnotherHeader", "A different value"),
                Parameters.one("headers", headerParams)
        );

        MatchResult actual = matcher.matchAgainstHeaders();

        assertFalse(actual.isExactMatch());
    }

}