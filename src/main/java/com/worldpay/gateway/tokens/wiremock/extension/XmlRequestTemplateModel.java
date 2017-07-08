package com.worldpay.gateway.tokens.wiremock.extension;

import com.github.tomakehurst.wiremock.extension.responsetemplating.ListOrSingle;
import com.github.tomakehurst.wiremock.extension.responsetemplating.RequestTemplateModel;
import com.github.tomakehurst.wiremock.extension.responsetemplating.UrlPath;
import com.github.tomakehurst.wiremock.http.Request;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * Decorates a {@link RequestTemplateModel} with a view of the request
 * body parsed as XML.
 *
 * <p>If the request body can't be parsed as XML, xmlBody will be null.
 */
class XmlRequestTemplateModel {
    private final RequestTemplateModel requestTemplateModel;
    private final Document xmlBody;

    private XmlRequestTemplateModel(RequestTemplateModel requestTemplateModel, Document xmlBody) {
        this.requestTemplateModel = requestTemplateModel;
        this.xmlBody = xmlBody;
    }

    /**
     * Creates a model of the request, including all fields from {@link RequestTemplateModel}
     * as well as the request body parsed as XML.
     *
     * @param request the request body
     * @return a model of the request body as described above
     */
    static XmlRequestTemplateModel from(final Request request) {
        RequestTemplateModel requestTemplateModel = RequestTemplateModel.from(request);

        Document xmlBody = null;
        if (requestTemplateModel.getBody() != null) {
            try {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
                DocumentBuilder builder = dbf.newDocumentBuilder();

                InputStream inputStream =
                        IOUtils.toInputStream(requestTemplateModel.getBody(), Charset.defaultCharset());
                xmlBody = builder.parse(inputStream);
            } catch (ParserConfigurationException | SAXException | IOException e) {
                // just don't have an XML body
            }
        }

        return new XmlRequestTemplateModel(requestTemplateModel, xmlBody);
    }

    public String getUrl() {
        return requestTemplateModel.getUrl();
    }

    public UrlPath getPath() {
        return requestTemplateModel.getPath();
    }

    public Map<String, ListOrSingle<String>> getQuery() {
        return requestTemplateModel.getQuery();
    }

    public Map<String, ListOrSingle<String>> getHeaders() {
        return requestTemplateModel.getHeaders();
    }

    public Map<String, ListOrSingle<String>> getCookies() {
        return requestTemplateModel.getCookies();
    }

    public String getBody() {
        return requestTemplateModel.getBody();
    }

    public Document getXmlBody() {
        return xmlBody;
    }
}
