package PriceMonitor.stock.NasdaqParser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.TemporalAccessor;

/**
 * Created by zhuoli on 7/28/16.
 */

/**
 * Nasdaq Website parser.
 */
public class NasdaqWebParser {

    final static String NasdaqBaseQuoteUrl = "http://www.nasdaq.com/symbol";

    final static String EarningReportUrl = "http://www.nasdaq.com/earnings/report";

    public double QuoteSymbolePrice(String symbol) {
        return Double.parseDouble(this.GetElementText(NasdaqWebParser.NasdaqBaseQuoteUrl + "/" + symbol, "qwidget_lastsale").replaceAll("[^\\d.]+", ""));
    }


    /**
     * Get EarningReport date using from Nasdaq site.
     *
     * @param symbol
     * @return
     */
    public LocalDate QupteEarningReportDate(String symbol) {
        String dateText = "";
        try {

            // Parse html to get target element
            Document dom = Jsoup.connect(NasdaqWebParser.EarningReportUrl + "/" + symbol).get();
            Element element = dom.getElementById("left-column-div");
            Node nd = element.childNodes().stream().filter(node -> node.nodeName().equals("h2")).findFirst().get();
            Element el = (Element) nd;

            // Clean date time text
            dateText = el.text().split(":")[1].trim();

            // Parse date time string
            return this.ParseEaringReportDate(dateText);
        } catch (Exception exc) {
            System.err.println(String.format("Can't resolve localdate string %1$s", dateText));
            return LocalDate.MAX;
        }
    }

    public String GetElementText(String url, String elementID) {
        try {

            Document dom = Jsoup.connect(url).get();
            Element element = dom.getElementById(elementID);
            if (element.hasText()) {
                return element.text();
            }
            return element.html();
        } catch (Exception exc) {
            return "";
        }
    }

    public LocalDate ParseEaringReportDate(String text) {
        DateTimeFormatter formatter = new DateTimeFormatterBuilder().appendPattern("MMM dd, yyyy").toFormatter();
        TemporalAccessor ta = formatter.parse(text);
        Instant instant = LocalDate.from(ta).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
        return instant.atZone(ZoneId.of("US/Eastern")).toLocalDate();
    }
}
