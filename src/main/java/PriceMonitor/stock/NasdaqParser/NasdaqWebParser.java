package PriceMonitor.stock.NasdaqParser;

import com.joanzapata.utils.Strings;
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
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    public Optional<LocalDate> QupteEarningReportDate(String symbol) {
        String dateText = "";
        String reportUrl = NasdaqWebParser.EarningReportUrl + "/" + symbol;
        try {

            // Parse html to get target element
            Document dom = Jsoup.connect(reportUrl).get();
            Element element = dom.getElementById("left-column-div");
            Node nd = element.childNodes().stream().filter(node -> node.nodeName().equals("h2")).findFirst().get();
            Element el = (Element) nd;

            // Clean date time text
            String[] strs = el.text().split(":");
            if (strs.length == 1) {
                return Optional.empty();
            }
            dateText = strs[1].trim();

            // Parse date time string
            return Optional.of(this.ParseEaringReportDate(dateText));
        } catch (java.net.SocketTimeoutException socketException) {
            Logger.getGlobal().log(Level.SEVERE, String.format("Timeout on accessing url: \"%s\"", reportUrl), socketException);
        } catch (Exception exc) {
            Logger.getGlobal().log(Level.WARNING, String.format("Can't resolve localdate string %1$s", dateText), exc);
        }
        return Optional.empty();
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
            Logger.getGlobal().log(Level.WARNING, Strings.format("Failed to resolve the ElementText of element ID : {id} from \"{url}\".").with("id", elementID).with("url", url).build(), exc);
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
