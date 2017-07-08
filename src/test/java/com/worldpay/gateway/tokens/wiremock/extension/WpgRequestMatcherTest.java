package com.worldpay.gateway.tokens.wiremock.extension;

import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.MatchResult;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.worldpay.gateway.tokens.wiremock.testsupport.MockRequest.mockRequest;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link WpgRequestMatcher}.
 */
public class WpgRequestMatcherTest {

    private WpgRequestMatcher matcher;

    @Before
    public void setUp() throws Exception {
        matcher = new WpgRequestMatcher();
    }

    @Test
    public void match_returnsNoMatchWhenInvalidParameterIncluded() {
        Parameters parameters = new Parameters();
        parameters.put("xmlLike", "<xml><with><some><elements and='someAttributes'/></some></with></xml>");
        parameters.put("xpath", Collections.singletonList("/xml/with/some"));
        parameters.put("url", "/a/test/url.html");
        parameters.put("method", "POST");

        Map<String, String> headerParams = new HashMap<>();
        headerParams.put("X-WP-CorrelationId", "TEST_CORRELATION");
        headerParams.put("Authorization", "Basic .*");
        parameters.put("headers", headerParams);

        parameters.put("invalid parameter", "some value");

        Request request = mockRequest()
                .body("<xml><with><some><elements and='FOO'/></some></with></xml>")
                .url("/a/test/url.html")
                .withHeader("X-WP-CorrelationId", "TEST_CORRELATION")
                .withHeader("Authorization", "Basic Z3cyX2FnZW50OnBhNTV3MHJk")
                .method(RequestMethod.POST);

        assertFalse(matcher.match(request, parameters).isExactMatch());
    }

    @Test
    public void match_multiParameterReturnsExactMatch() {
        Parameters parameters = new Parameters();
        parameters.put("xmlLike", "<xml><with><some><elements and='someAttributes'/></some></with></xml>");
        parameters.put("xpath", Collections.singletonList("/xml/with/some/elements[@and='FOO']"));
        parameters.put("url", "/a/test/url.html");
        parameters.put("method", "POST");

        Map<String, String> headerParams = new HashMap<>();
        headerParams.put("X-WP-CorrelationId", "TEST_CORRELATION");
        headerParams.put("Authorization", "Basic .*");
        parameters.put("headers", headerParams);

        Request request = mockRequest()
                .body("<xml><with><some><elements and='FOO'/></some></with></xml>")
                .url("/a/test/url.html")
                .withHeader("X-WP-CorrelationId", "TEST_CORRELATION")
                .withHeader("Authorization", "Basic Z3cyX2FnZW50OnBhNTV3MHJk")
                .method(RequestMethod.POST);

        MatchResult actual = matcher.match(request, parameters);

        assertTrue(actual.isExactMatch());
    }

    @Test
    public void match_multiParameterReturnsNoMatchForOneWrongParameter() {
        Parameters parameters = new Parameters();
        parameters.put("xmlLike", "<xml><with><some><elements and='someAttributes'/></some></with></xml>");
        parameters.put("xpath", Collections.singletonList("/xml/with/some"));
        parameters.put("url", "/a/wrong/test/url.html");
        parameters.put("method", "POST");

        Map<String, String> headerParams = new HashMap<>();
        headerParams.put("X-WP-CorrelationId", "TEST_CORRELATION");
        headerParams.put("Authorization", "Basic .*");
        parameters.put("headers", headerParams);

        Request request = mockRequest()
                .body("<xml><with><some><elements and='FOO'/></some></with></xml>")
                .url("/a/test/url.html")
                .withHeader("X-WP-CorrelationId", "TEST_CORRELATION")
                .method(RequestMethod.POST);

        MatchResult actual = matcher.match(request, parameters);

        assertFalse(actual.isExactMatch());
    }

    @Test
    public void match_returnsNoMatchForInvalidXml() {
        Parameters parameters
                = Parameters.one("xmlLike", "<xml><with><some><elements and='someAttributes'/></some></with></xml>");
        Request request = mockRequest().body("INVALID XML");

        MatchResult actual = matcher.match(request, parameters);

        assertFalse(actual.isExactMatch());
    }

    @Test
    public void getName_returnsCorrectName() {
        assertEquals("wpgMatcher", matcher.getName());
    }
}
