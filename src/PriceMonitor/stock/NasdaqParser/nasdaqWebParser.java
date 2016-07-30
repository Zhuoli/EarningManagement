package src.PriceMonitor.stock.NasdaqParser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Created by zhuoli on 7/28/16.
 */
public class NasdaqWebParser {

    final static String NasdaqBaseQuoteUrl = "http://www.nasdaq.com/symbol";

    public double QuoteSymbolePrice(String symbol) {
        try {

            Document dom = Jsoup.connect(NasdaqWebParser.NasdaqBaseQuoteUrl + "/" + symbol).get();
            Element element = dom.getElementById("qwidget_lastsale");
            if (element.hasText()) {
                return Double.parseDouble(element.text().replaceAll("[^\\d.]+", ""));
            }
            return -1;
        } catch (Exception exc) {
            return -1;
        }
    }
}
