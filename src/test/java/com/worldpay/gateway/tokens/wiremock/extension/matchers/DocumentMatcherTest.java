package com.worldpay.gateway.tokens.wiremock.extension.matchers;

import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.TextFile;
import com.github.tomakehurst.wiremock.core.WireMockApp;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.matching.MatchResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.xml.sax.SAXParseException;

import java.util.Arrays;
import java.util.Collections;

import static com.worldpay.gateway.tokens.wiremock.testsupport.MockRequest.mockRequest;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DocumentMatcherTest {

    @Mock private FileSource fileSource;
    @Mock private TextFile textFile;
    
    @Test(expected = SAXParseException.class)
    public void documentMatcher_throwsExceptionForInvalidXmlBody() throws Exception {
        new DocumentMatcher(
                fileSource,
                mockRequest().body("INVALID XML BODY"),
                Parameters.one("xpath", Collections.singletonList("/xml/with/some"))
        );
    }

    @Test
    public void matchAgainstXpath_returnsExactMatch() throws Exception {
        DocumentMatcher matcher = new DocumentMatcher(
                fileSource,
                mockRequest().body("<xml><with><some><elements and='someAttributes'/></some></with></xml>"),
                Parameters.one("xpath", Collections.singletonList("/xml/with/some"))
        );

        MatchResult actual = matcher.matchAgainstXpath();

        assertTrue(actual.isExactMatch());
    }

    @Test
    public void matchAgainstXpath_returnsExactMatchForMultipleXpaths() throws Exception {
        DocumentMatcher matcher = new DocumentMatcher(
                fileSource,
                mockRequest().body("<xml><with><some><elements and='someAttributes'/></some></with></xml>"),
                Parameters.one("xpath",
                        Arrays.asList(
                                "/xml/with/some",
                                "/xml/with/some/elements",
                                "/xml/with/some/elements/@and"
                        )
                )
        );

        MatchResult actual = matcher.matchAgainstXpath();

        assertTrue(actual.isExactMatch());
    }

    @Test
    public void matchAgainstXpath_returnsExactMatchWithAttributeValue() throws Exception {
        DocumentMatcher matcher = new DocumentMatcher(
                fileSource,
                mockRequest().body("<xml><with><some><elements and='someAttributes'/></some></with></xml>"),
                Parameters.one("xpath", Collections.singletonList("/xml/with/some/elements[@and='someAttributes']"))
        );

        MatchResult actual = matcher.matchAgainstXpath();

        assertTrue(actual.isExactMatch());
    }

    @Test
    public void matchAgainstXpath_returnsNoMatchWithOneWrongXpathAmongMultiple() throws Exception {
        DocumentMatcher matcher = new DocumentMatcher(
                fileSource,
                mockRequest().body("<xml><with><some><elements and='someAttributes'/></some></with></xml>"),
                Parameters.one("xpath",
                        Arrays.asList(
                                "/xml/with/some",
                                "/xml/with/some/other/elements",
                                "/xml/with/some/elements/@and"
                        )
                )
        );

        MatchResult actual = matcher.matchAgainstXpath();

        assertFalse(actual.isExactMatch());
    }

    @Test
    public void matchAgainstXpath_returnsNoMatch() throws Exception {
        DocumentMatcher matcher = new DocumentMatcher(
                fileSource,
                mockRequest().body("<xml><with><some><elements and='someAttributes'/></some></with></xml>"),
                Parameters.one("xpath", Collections.singletonList("/xml/with/none"))
        );

        MatchResult actual = matcher.matchAgainstXpath();

        assertFalse(actual.isExactMatch());
    }

    @Test
    public void matchAgainstXpath_returnsExactMatchWhenParameterNotPresent() throws Exception {
        DocumentMatcher matcher = new DocumentMatcher(
                fileSource,
                mockRequest(),
                Parameters.empty()
        );

        MatchResult actual = matcher.matchAgainstXpath();

        assertTrue(actual.isExactMatch());
    }

    @Test
    public void matchAgainstXpath_returnsNoMatchOnBrokenXpath() throws Exception {
        DocumentMatcher matcher = new DocumentMatcher(
                fileSource,
                mockRequest().body("<xml><with><some><elements and='someAttributes'/></some></with></xml>"),
                Parameters.one("xpath", Collections.singletonList("/xml\\with/some"))
        );

        MatchResult actual = matcher.matchAgainstXpath();

        assertFalse(actual.isExactMatch());
    }

    @Test
    public void matchAgainstXpath_returnsNoMatchWhenNoXmlBodySupplied() throws Exception {
        DocumentMatcher matcher = new DocumentMatcher(
                fileSource,
                mockRequest(),
                Parameters.one("xpath", Collections.singletonList("/xml/with/some"))
        );

        MatchResult actual = matcher.matchAgainstXpath();

        assertFalse(actual.isExactMatch());
    }

    @Test
    public void matchAgainstXmlLike_returnsExactMatch() throws Exception {
        DocumentMatcher matcher = new DocumentMatcher(
                fileSource,
                mockRequest().body("<some><kind/><of><xml with=\"attributes\"/></of></some>"),
                Parameters.one("xmlLike", "<some><kind/><of><xml with=\"attributes\"/></of></some>")
        );

        MatchResult actual = matcher.matchAgainstXmlLike();

        assertTrue(actual.isExactMatch());
    }

    @Test
    public void matchAgainstXmlLike_returnsExactMatchWithDifferentElementValues() throws Exception {
        DocumentMatcher matcher = new DocumentMatcher(
                fileSource,
                mockRequest().body("<some><kind/><of>FOO</of><xml>BAR</xml></some>"),
                Parameters.one("xmlLike", "<some><kind/><of>SOME</of><xml>THING</xml></some>")
        );

        MatchResult actual = matcher.matchAgainstXmlLike();

        assertTrue(actual.isExactMatch());
    }

    @Test
    public void matchAgainstXmlLike_returnsExactMatchWithDifferentAttributeValues() throws Exception {
        DocumentMatcher matcher = new DocumentMatcher(
                fileSource,
                mockRequest().body("<some><kind/><of><xml with=\"FOO\" and=\"BAR\"/></of></some>"),
                Parameters.one("xmlLike", "<some><kind/><of><xml with=\"attributes\" and=\"other\"/></of></some>")
        );

        MatchResult actual = matcher.matchAgainstXmlLike();

        assertTrue(actual.isExactMatch());
    }

    @Test
    public void matchAgainstXmlLike_returnsExactMatchWithDifferentAttributeOrders() throws Exception {
        DocumentMatcher matcher = new DocumentMatcher(
                fileSource,
                mockRequest().body("<some><kind/><of><xml and=\"BAR\" with=\"FOO\"/></of></some>"),
                Parameters.one("xmlLike", "<some><kind/><of><xml with=\"attributes\" and=\"other\"/></of></some>")
        );

        MatchResult actual = matcher.matchAgainstXmlLike();

        assertTrue(actual.isExactMatch());
    }

    @Test
    public void matchAgainstXmlLike_returnsNoMatchForInvalidParameter() throws Exception {
        DocumentMatcher matcher = new DocumentMatcher(
                fileSource,
                mockRequest().body("<some><kind/><of><xml with=\"attributes\"/></of></some>"),
                Parameters.one("xmlLike", "INVALID XML")
        );

        MatchResult actual = matcher.matchAgainstXmlLike();

        assertFalse(actual.isExactMatch());
    }

    @Test
    public void matchAgainstXmlLike_returnsNoMatchForDifferentElement() throws Exception {
        DocumentMatcher matcher = new DocumentMatcher(
                fileSource,
                mockRequest().body("<some><DIFFERENT_kind/><of><xml/></of></some>"),
                Parameters.one("xmlLike", "<some><kind/><of><xml/></of></some>")
        );

        MatchResult actual = matcher.matchAgainstXmlLike();

        assertFalse(actual.isExactMatch());
    }

    @Test
    public void matchAgainstXmlLike_returnsNoMatchForDifferentRootElement() throws Exception {
        DocumentMatcher matcher = new DocumentMatcher(
                fileSource,
                mockRequest().body("<DIFFERENT_some><kind/><of><xml/></of></DIFFERENT_some>"),
                Parameters.one("xmlLike", "<some><kind/><of><xml/></of></some>")
        );

        MatchResult actual = matcher.matchAgainstXmlLike();

        assertFalse(actual.isExactMatch());
    }

    @Test
    public void matchAgainstXmlLike_returnsNoMatchForExtraElement() throws Exception {
        DocumentMatcher matcher = new DocumentMatcher(
                fileSource,
                mockRequest().body("<some><EXTRA/><kind/><of><xml/></of></some>"),
                Parameters.one("xmlLike", "<some><kind/><of><xml/></of></some>")
        );

        MatchResult actual = matcher.matchAgainstXmlLike();

        assertFalse(actual.isExactMatch());
    }

    @Test
    public void matchAgainstXmlLike_returnsNoMatchForDifferentElementOrder() throws Exception {
        DocumentMatcher matcher = new DocumentMatcher(
                fileSource,
                mockRequest().body("<some><kind/><xml/><of/></some>"),
                Parameters.one("xmlLike", "<some><kind/><of/><xml/></some>")
        );

        MatchResult actual = matcher.matchAgainstXmlLike();

        assertFalse(actual.isExactMatch());
    }

    @Test
    public void matchAgainstXmlLike_returnsNoMatchForDifferentAttribute() throws Exception {
        DocumentMatcher matcher = new DocumentMatcher(
                fileSource,
                mockRequest().body("<some><kind/><of><xml DIFFERENT_with=\"attributes\"/></of></some>"),
                Parameters.one("xmlLike", "<some><kind/><of><xml with=\"attributes\"/></of></some>")
        );

        MatchResult actual = matcher.matchAgainstXmlLike();

        assertFalse(actual.isExactMatch());
    }

    @Test
    public void matchAgainstXmlLike_returnsNoMatchForExtraAttribute() throws Exception {
        DocumentMatcher matcher = new DocumentMatcher(
                fileSource,
                mockRequest().body("<some><kind/><of><xml with=\"attributes\" extra=\"EXTRA\"/></of></some>"),
                Parameters.one("xmlLike", "<some><kind/><of><xml with=\"attributes\"/></of></some>")
        );

        MatchResult actual = matcher.matchAgainstXmlLike();

        assertFalse(actual.isExactMatch());
    }

    @Test
    public void matchAgainstXmlLike_returnsNoMatchForMissingAttribute() throws Exception {
        DocumentMatcher matcher = new DocumentMatcher(
                fileSource,
                mockRequest().body("<some><kind/><of><xml/></of></some>"),
                Parameters.one("xmlLike", "<some><kind/><of><xml with=\"attributes\"/></of></some>")
        );

        MatchResult actual = matcher.matchAgainstXmlLike();

        assertFalse(actual.isExactMatch());
    }

    @Test
    public void matchAgainstXmlLike_returnsExactMatchWhenParameterNotPresent() throws Exception {
        DocumentMatcher matcher = new DocumentMatcher(
                fileSource,
                mockRequest(),
                Parameters.empty()
        );

        MatchResult actual = matcher.matchAgainstXmlLike();

        assertTrue(actual.isExactMatch());
    }

    @Test
    public void matchAgainstXmlLikeFile_returnsExactMatch() throws Exception {
        when(fileSource.child(WireMockApp.FILES_ROOT)).thenReturn(fileSource);
        when(fileSource.getTextFileNamed("test.xml")).thenReturn(textFile);
        when(textFile.readContentsAsString()).thenReturn("<some><kind/><of><xml with=\"attributes\"/></of></some>");

        DocumentMatcher matcher = new DocumentMatcher(
                fileSource,
                mockRequest().body("<some><kind/><of><xml with=\"attributes\"/></of></some>"),
                Parameters.one("xmlLikeFile", "test.xml")
        );

        MatchResult actual = matcher.matchAgainstXmlLikeFile();

        assertTrue(actual.isExactMatch());
    }

    @Test
    public void matchAgainstXmlLikeFile_returnsExactMatchWhenParameterNotPresent() throws Exception {
        DocumentMatcher matcher = new DocumentMatcher(
                fileSource,
                mockRequest(),
                Parameters.empty()
        );

        MatchResult actual = matcher.matchAgainstXmlLikeFile();

        assertTrue(actual.isExactMatch());
    }

}