package com.worldpay.gateway.tokens.wiremock.extension;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link HelperSource}.
 */
public class HelperSourceTest {

    private HelperSource helperSource = new HelperSource();

    @Test
    public void randomDigits_returnsNumberOfDigitsRequested() throws Exception {
        String digits = helperSource.randomDigits(5);
        assertEquals(5, digits.length());
    }

    @Test
    public void randomDigits_returnsOnlyDigits() throws Exception {
        String digits = helperSource.randomDigits(20);
        assertTrue(digits.matches("^\\d+$"));
    }

    @Test
    public void randomDigits_returnsEmptyStringIfZeroDigitsRequested() throws Exception {
        String digits = helperSource.randomDigits(0);
        assertEquals("", digits);
    }

    @Test
    public void xmlDate_returnsDateIncludingDayOfMonth() throws Exception {
        String xmlDate = helperSource.xmlDate(10).toString();

        assertNotNull(parseXml(xmlDate, "/date/@dayOfMonth"));
    }

    @Test
    public void xmlDate_returnsDateIncludingMonth() throws Exception {
        String xmlDate = helperSource.xmlDate(10).toString();

        assertNotNull(parseXml(xmlDate, "/date/@month"));
    }

    @Test
    public void xmlDate_returnsDateIncludingYear() throws Exception {
        String xmlDate = helperSource.xmlDate(10).toString();

        assertNotNull(parseXml(xmlDate,"/date/@year"));
    }

    @Test
    public void xmlDate_returnsDateIncludingHour() throws Exception {
        String xmlDate = helperSource.xmlDate(10).toString();

        assertNotNull(parseXml(xmlDate, "/date/@hour"));
    }

    @Test
    public void xmlDate_returnsDateIncludingMinute() throws Exception {
        String xmlDate = helperSource.xmlDate(10).toString();

        assertNotNull(parseXml(xmlDate,"/date/@minute"));
    }

    @Test
    public void xmlDate_returnsDateIncludingSecond() throws Exception {
        String xmlDate = helperSource.xmlDate(10).toString();

        assertNotNull(parseXml(xmlDate, "/date/@second"));
    }

    private Object parseXml(String xmlDate, String expression)
            throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        InputStream inputStream = IOUtils.toInputStream(xmlDate, Charset.defaultCharset());
        Document xml = builder.parse(inputStream);

        XPath xpath = XPathFactory.newInstance().newXPath();
        return xpath.evaluate(expression, xml, XPathConstants.STRING);
    }

    @Test
    public void xpath_returnsStringForXmlAttribute() throws Exception {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        InputStream inputStream =
                IOUtils.toInputStream("<test><xml><with aString='TEST_RESULT'/></xml></test>", Charset.defaultCharset());
        Document xml = builder.parse(inputStream);

        String actual = helperSource.xpath(xml, "/test/xml/with/@aString");
        assertEquals("TEST_RESULT", actual);
    }

    @Test
    public void xpath_returnsStringForXmlElement() throws Exception {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        InputStream inputStream =
                IOUtils.toInputStream("<test><xml><with>TEST_RESULT</with></xml></test>", Charset.defaultCharset());
        Document xml = builder.parse(inputStream);

        String actual = helperSource.xpath(xml, "/test/xml/with/text()");
        assertEquals("TEST_RESULT", actual);
    }

    @Test
    public void xpath_returnsBlankStringForNoXpathMatch() throws Exception {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        InputStream inputStream =
                IOUtils.toInputStream("<test><xml><with aString='TEST_RESULT'/></xml></test>", Charset.defaultCharset());
        Document xml = builder.parse(inputStream);

        String actual = helperSource.xpath(xml, "/test/xml/withOut/@aString");
        assertEquals("", actual);
    }

    @Test(expected = IOException.class)
    public void xpath_throwsExceptionForBrokenExpression() throws Exception {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        InputStream inputStream =
                IOUtils.toInputStream("<test><xml><with aString='TEST_RESULT'/></xml></test>", Charset.defaultCharset());
        Document xml = builder.parse(inputStream);

        helperSource.xpath(xml, "/test\\xml/with/aString");
    }
}
