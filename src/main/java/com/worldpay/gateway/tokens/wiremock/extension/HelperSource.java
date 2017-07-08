package com.worldpay.gateway.tokens.wiremock.extension;

import com.github.jknack.handlebars.Handlebars;
import org.w3c.dom.Document;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;


/**
 * Handlebars helpers needed for WPG response generation.
 */
public class HelperSource {

    private final XPathFactory xpathFactory;

    public HelperSource() {
        this.xpathFactory = XPathFactory.newInstance();
    }

    /**
     * Applies an XPath expression to the context, which must be a {@link Document}.
     *
     * @param context a Document instance on which to perform the XPath expression
     * @param expression the XPath expression to perform
     * @return a String resulting from the XPath expression
     * @throws IOException wrapping the {@link XPathExpressionException} from XPath.evaluate, if any
     */
    public String xpath(Document context, String expression) throws IOException {
        try {
            return (String) xpathFactory.newXPath().evaluate(expression, context, XPathConstants.STRING);
        } catch (XPathExpressionException e) {
            throw new IOException(e);
        }
    }

    /**
     * Generates a string of {@code count} random digits.
     *
     * @param count how many digits to generate
     * @return a String of {@code count} random digits
     */
    public String randomDigits(int count) {
        StringBuilder sb = new StringBuilder(count);
        Random random = new Random();

        for (int i = 0; i < count; i++) {
            sb.append(random.nextInt(10));
        }

        return sb.toString();
    }

    private static final ThreadLocal<SimpleDateFormat> simpleDateFormat = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("'<date dayOfMonth=\"'dd'\" month=\"'MM'\" year=\"'yyyy'\" "
                    + "hour=\"'HH'\" minute=\"'mm'\" second=\"'ss'\"/>'");
        }
    };

    /**
     * Generates a date in WPG XML format, e.g.:
     * {@literal <date dayOfMonth="26" month="06" year="2017" hour="09" minute="56" second="38"/>}
     *
     * @param daysFromNow number of days to add to the current date to generate a new date
     * @return a date in WPG XML format
     */
    public Handlebars.SafeString xmlDate(int daysFromNow) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, daysFromNow);
        return new Handlebars.SafeString(simpleDateFormat.get().format(calendar.getTime()));
    }
}
