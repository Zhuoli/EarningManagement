package PriceMonitor.stock.NasdaqParser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.time.LocalDate;

/**
 * Created by zhuoli on 7/28/16.
 */

/**
 * Nasdaq Website parser.
 */
public class NasdaqWebParser {
    // sdfs
    final static String NasdaqBaseQuoteUrl = "http://www.nasdaq.com/symbol";

    final static String EarningReportUrl = "http://www.nasdaq.com/earnings/report";

    public double QuoteSymbolePrice(String symbol) {
        return Double.parseDouble(this.GetElementText(NasdaqWebParser.NasdaqBaseQuoteUrl + "/" + symbol, "qwidget_lastsale").replaceAll("[^\\d.]+", ""));
    }

    public LocalDate QupteEarningReportDate(String symbol) {
        String text = this.GetElementText(NasdaqWebParser.EarningReportUrl + "/" + symbol, "left-column-div");
        return LocalDate.parse(text);
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
}
