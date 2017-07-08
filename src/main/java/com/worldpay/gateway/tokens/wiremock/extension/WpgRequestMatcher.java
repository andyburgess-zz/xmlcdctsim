package com.worldpay.gateway.tokens.wiremock.extension;

import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.matching.MatchResult;
import com.github.tomakehurst.wiremock.matching.RequestMatcherExtension;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.worldpay.gateway.tokens.wiremock.WpgWireMockServerRunner;
import com.worldpay.gateway.tokens.wiremock.extension.matchers.DocumentMatcher;
import com.worldpay.gateway.tokens.wiremock.extension.matchers.SimpleMatcher;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import static com.github.tomakehurst.wiremock.matching.MatchResult.aggregate;
import static com.github.tomakehurst.wiremock.matching.MatchResult.noMatch;

/**
 * A custom matcher which checks the request body for XML against an XPath
 * matcher, but doesn't validate the DTD in the process. Needed because the
 * DTD can't be (easily) resolved from inside a Docker container.
 *
 * <p>Because WireMock uses a customMatcher <em>instead of</em> the regular
 * matchers, we also have to implement customer matching routines for everything
 * we want to match on, which currently includes the request method, url and
 * correlationId, as well as the request body as XML.
 */
public class WpgRequestMatcher extends RequestMatcherExtension {

    private static final Set<String> supportedParams =
            ImmutableSet.of("method", "url", "headers", "xpath", "xmlLike", "xmlLikeFile");
    private final FileSource fileSource;

    /**
     * Creates an instance with a pre-configured {@link DocumentBuilderFactory} and
     * {@link XPathFactory}.
     *
     * @throws ParserConfigurationException when unable to instantiate a {@link DocumentBuilderFactory}
     */
    public WpgRequestMatcher() throws ParserConfigurationException {
        fileSource = WpgWireMockServerRunner.getOptions().filesRoot();
    }

    @Override
    public MatchResult match(Request request, Parameters parameters) {
        if (!supportedParams.containsAll(parameters.keySet())) {
            return noMatch();
        }

        SimpleMatcher simpleMatcher = new SimpleMatcher(request, parameters);
        List<MatchResult> results = Lists.newArrayList(
                simpleMatcher.matchAgainstMethod(),
                simpleMatcher.matchAgainstUrl(),
                simpleMatcher.matchAgainstHeaders()
        );

        // only run the Document matchers if every other matcher has returned
        // a successful match. avoids building a DOM if it's not necessary.
        if (aggregate(results).isExactMatch()) {
            try {
                DocumentMatcher documentMatcher = new DocumentMatcher(fileSource, request, parameters);
                results.addAll(Lists.newArrayList(
                        documentMatcher.matchAgainstXpath(),
                        documentMatcher.matchAgainstXmlLike(),
                        documentMatcher.matchAgainstXmlLikeFile()
                ));
            } catch (ParserConfigurationException | SAXException | IOException e) {
                results.add(noMatch());
            }
        }

        return aggregate(results);
    }

    @Override
    public String getName() {
        return "wpgMatcher";
    }
}
