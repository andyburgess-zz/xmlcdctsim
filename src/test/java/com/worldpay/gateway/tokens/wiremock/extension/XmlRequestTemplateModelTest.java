package com.worldpay.gateway.tokens.wiremock.extension;

import com.github.tomakehurst.wiremock.extension.responsetemplating.ListOrSingle;
import com.github.tomakehurst.wiremock.extension.responsetemplating.UrlPath;
import com.github.tomakehurst.wiremock.http.Request;
import org.junit.Test;
import org.w3c.dom.Document;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.util.HashMap;
import java.util.Map;

import static com.worldpay.gateway.tokens.wiremock.testsupport.MockRequest.mockRequest;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link XmlRequestTemplateModel}.
 */
public class XmlRequestTemplateModelTest {

    @Test
    public void from_parsesXmlWithValidRequest() throws Exception {
        Request request = mockRequest().body("<xml><with><some><elements and='someAttributes'/></some></with></xml>");
        XmlRequestTemplateModel model = XmlRequestTemplateModel.from(request);
        Document xmlBody = model.getXmlBody();

        XPath xpath = XPathFactory.newInstance().newXPath();
        String actual = (String) xpath.evaluate("/xml/with/some/elements/@and", xmlBody, XPathConstants.STRING);

        assertEquals("someAttributes", actual);
    }

    @Test
    public void from_parsesXmlWithInvalidRequest() throws Exception {
        Request request = mockRequest().body("NOT VALID XML");
        XmlRequestTemplateModel model = XmlRequestTemplateModel.from(request);

        assertNull(model.getXmlBody());
    }

    @Test
    public void getUrl_returnsCorrectUrl() {
        Request request = mockRequest()
                .body("<xml><with><some><elements and='someAttributes'/></some></with></xml>")
                .url("TEST_URL?queryKey=queryValue");
        XmlRequestTemplateModel model = XmlRequestTemplateModel.from(request);
        assertEquals("TEST_URL?queryKey=queryValue", model.getUrl());
    }

    @Test
    public void getPath_returnsCorrectPath() {
        Request request = mockRequest()
                .body("<xml><with><some><elements and='someAttributes'/></some></with></xml>")
                .url("TEST_URL?queryKey=queryValue");
        XmlRequestTemplateModel model = XmlRequestTemplateModel.from(request);
        assertEquals(new UrlPath("TEST_URL"), model.getPath());
    }

    @Test
    public void getQuery_returnsCorrectQuery() {
        Request request = mockRequest()
                .body("<xml><with><some><elements and='someAttributes'/></some></with></xml>")
                .url("TEST_URL?queryKey=queryValue");
        XmlRequestTemplateModel model = XmlRequestTemplateModel.from(request);

        Map<String, ListOrSingle<String>> expected = new HashMap<>();
        expected.put("queryKey", new ListOrSingle<>("queryValue"));

        assertEquals(expected.entrySet(), model.getQuery().entrySet());
    }

    @Test
    public void getHeaders_returnsCorrectHeaders() {
        Request request = mockRequest()
                .body("<xml><with><some><elements and='someAttributes'/></some></with></xml>")
                .withHeader("TEST HEADER KEY", "TEST HEADER VALUE");
        XmlRequestTemplateModel model = XmlRequestTemplateModel.from(request);

        Map<String, ListOrSingle<String>> expected = new HashMap<>();
        expected.put("TEST HEADER KEY", new ListOrSingle<>("TEST HEADER VALUE"));

        assertThat(model.getHeaders().entrySet(), equalTo(expected.entrySet()));
    }

    @Test
    public void getCookies_returnsCorrectCookies() {
        Request request = mockRequest()
                .body("<xml><with><some><elements and='someAttributes'/></some></with></xml>")
                .cookie("TEST COOKIE", "TEST COOKIE VALUE");
        XmlRequestTemplateModel model = XmlRequestTemplateModel.from(request);

        Map<String, ListOrSingle<String>> expected = new HashMap<>();
        expected.put("TEST COOKIE", new ListOrSingle<>("TEST COOKIE VALUE"));
        assertThat(model.getCookies().entrySet(), equalTo(expected.entrySet()));
    }

    @Test
    public void getBody_returnsCorrectBody() {
        Request request = mockRequest().body("<xml><with><some><elements and='someAttributes'/></some></with></xml>");
        XmlRequestTemplateModel model = XmlRequestTemplateModel.from(request);
        assertEquals("<xml><with><some><elements and='someAttributes'/></some></with></xml>", model.getBody());
    }

}
