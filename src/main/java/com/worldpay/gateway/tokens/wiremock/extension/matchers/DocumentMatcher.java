package com.worldpay.gateway.tokens.wiremock.extension.matchers;

import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.TextFile;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.matching.MatchResult;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
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
import java.util.List;

import static com.github.tomakehurst.wiremock.core.WireMockApp.FILES_ROOT;
import static com.github.tomakehurst.wiremock.matching.MatchResult.aggregate;
import static com.github.tomakehurst.wiremock.matching.MatchResult.exactMatch;
import static com.github.tomakehurst.wiremock.matching.MatchResult.noMatch;
import static java.util.stream.Collectors.toList;
import static org.w3c.dom.Node.ELEMENT_NODE;

public class DocumentMatcher {
    private final DocumentBuilderFactory documentBuilderFactory;
    private final XPathFactory xpathFactory;

    private final Document xmlBody;
    private final Parameters parameters;
    private FileSource fileSource;

    public DocumentMatcher(FileSource fileSource, Request request, Parameters parameters)
            throws ParserConfigurationException, IOException, SAXException {
        documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory
                .setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        xpathFactory = XPathFactory.newInstance();

        this.fileSource = fileSource;
        this.parameters = parameters;
        this.xmlBody = parseAsXml(request.getBodyAsString());
    }

    @SuppressWarnings("unchecked")
    public MatchResult matchAgainstXpath() {
        if (!parameters.containsKey("xpath")) {
            return exactMatch();
        }

        if (xmlBody != null) {
            XPath xpath = xpathFactory.newXPath();

            return aggregate(
                    ((List<String>) parameters.get("xpath")).stream()
                            .map(xpathExpr -> matchAgainstSingleXpath(xpath, xpathExpr, xmlBody))
                            .collect(toList()));
        }

        return noMatch();
    }

    public MatchResult matchAgainstXmlLike() {
        if (!parameters.containsKey("xmlLike")) {
            return exactMatch();
        }

        return matchAgainstXmlLikeInternal(parameters.getString("xmlLike"));
    }

    public MatchResult matchAgainstXmlLikeFile() {
        if (!parameters.containsKey("xmlLikeFile")) {
            return exactMatch();
        }

        FileSource filesRoot = fileSource.child(FILES_ROOT);
        TextFile file = filesRoot.getTextFileNamed(parameters.getString("xmlLikeFile"));

        return matchAgainstXmlLikeInternal(file.readContentsAsString());
    }

    private MatchResult matchAgainstXmlLikeInternal(String xmlLikeStr) {
        try {
            Document xmlLike = parseAsXml(xmlLikeStr);

            return MatchResult.of(xmlStructureMatch(xmlBody, xmlLike));
        } catch (ParserConfigurationException | IOException | SAXException e) {
            return noMatch();
        }
    }

    private Document parseAsXml(String text) throws ParserConfigurationException, IOException, SAXException {
        if (text != null) {
            DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
            InputStream inputStream = IOUtils.toInputStream(text, Charset.defaultCharset());
            return builder.parse(inputStream);
        }

        return null;
    }

    private MatchResult matchAgainstSingleXpath(XPath xpath, String xpathExpr, Document xmlBody) {
        try {
            NodeList nodeList = (NodeList)xpath.evaluate(xpathExpr, xmlBody, XPathConstants.NODESET);

            return MatchResult.of(nodeList.getLength() > 0);
        } catch (XPathExpressionException e) {
            return noMatch();
        }
    }

    private boolean xmlStructureMatch(Node left, Node right) {
        boolean matched =
                StringUtils.equals(left.getNodeName(), right.getNodeName())
                        && checkAttributes(left, right)
                        && countElements(left.getFirstChild()) == countElements(right.getFirstChild());

        Node leftElement = getNextElement(left.getFirstChild());
        Node rightElement = getNextElement(right.getFirstChild());

        while (leftElement != null && rightElement != null) {
            matched = matched && xmlStructureMatch(leftElement, rightElement);

            leftElement = getNextElement(leftElement.getNextSibling());
            rightElement = getNextElement(rightElement.getNextSibling());
        }

        return matched;
    }

    private boolean checkAttributes(Node left, Node right) {
        NamedNodeMap leftAttrs = left.getAttributes();
        NamedNodeMap rightAttrs = right.getAttributes();

        if (leftAttrs != null) {
            if (leftAttrs.getLength() != rightAttrs.getLength()) {
                return false;
            }

            for (int i = 0; i < leftAttrs.getLength(); i++) {
                if (rightAttrs.getNamedItem(leftAttrs.item(i).getNodeName()) == null) {
                    return false;
                }
            }
        }

        return true;
    }

    private Node getNextElement(Node node) {
        Node iterator;
        for (iterator = node;
             iterator != null && iterator.getNodeType() != ELEMENT_NODE;
             iterator = iterator.getNextSibling());

        return iterator;
    }

    private int countElements(Node node) {
        int count = 0;

        for (Node iterator = node; iterator != null; iterator = iterator.getNextSibling()) {
            if (iterator.getNodeType() == ELEMENT_NODE) {
                count++;
            }
        }

        return count;
    }
}
