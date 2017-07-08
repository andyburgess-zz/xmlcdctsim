package com.worldpay.gateway.tokens.wiremock.testsupport;

import com.github.tomakehurst.wiremock.common.Urls;
import com.github.tomakehurst.wiremock.http.ContentTypeHeader;
import com.github.tomakehurst.wiremock.http.Cookie;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.QueryParameter;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestMethod;

import java.net.URI;
import java.util.Map;
import java.util.Set;

import static com.github.tomakehurst.wiremock.http.HttpHeader.httpHeader;
import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.collect.Iterables.tryFind;
import static com.google.common.collect.Maps.newHashMap;

/**
 * Mocks out a {@link Request} for tests.
 */
public class MockRequest implements Request {

    private String url = "/";
    private RequestMethod method = RequestMethod.ANY;
    private HttpHeaders headers = new HttpHeaders();
    private Map<String, Cookie> cookies = newHashMap();
    private byte[] body;

    public static MockRequest mockRequest() {
        return new MockRequest();
    }

    public MockRequest url(String url) {
        this.url = url;
        return this;
    }

    public MockRequest method(RequestMethod method) {
        this.method = method;
        return this;
    }

    public MockRequest withHeader(String key, String values) {
        headers = headers.plus(httpHeader(key, values));
        return this;
    }

    public MockRequest cookie(String key, String value) {
        cookies.put(key, new Cookie(value));
        return this;
    }

    public MockRequest body(String body) {
        this.body = body.getBytes(UTF_8);
        return this;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String getAbsoluteUrl() {
        return "http://my.domain" + url;
    }

    @Override
    public RequestMethod getMethod() {
        return method;
    }

    @Override
    public String getClientIp() {
        return "1.1.1.1";
    }

    @Override
    public String getHeader(String key) {
        return header(key).firstValue();
    }

    @Override
    public HttpHeader header(final String key) {
        return tryFind(headers.all(), input -> input != null && input.keyEquals(key)).or(HttpHeader.absent(key));
    }

    @Override
    public ContentTypeHeader contentTypeHeader() {
        return ContentTypeHeader.absent();
    }

    @Override
    public HttpHeaders getHeaders() {
        return headers;
    }

    @Override
    public boolean containsHeader(String key) {
        return headers.getHeader(key).isPresent();
    }

    @Override
    public Set<String> getAllHeaderKeys() {
        return getHeaders().keys();
    }

    @Override
    public Map<String, Cookie> getCookies() {
        return cookies;
    }

    @Override
    public QueryParameter queryParameter(String key) {
        Map<String, QueryParameter> queryParams = Urls.splitQuery(URI.create(url));
        return queryParams.get(key);
    }

    @Override
    public byte[] getBody() {
        return body;
    }

    @Override
    public String getBodyAsString() {
        return body != null ? new String(body) : null;
    }

    @Override
    public String getBodyAsBase64() {
        return "";
    }

    @Override
    public boolean isBrowserProxyRequest() {
        return false;
    }
}
