package PriceMonitor.stock.NasdaqParser;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by zhuoli on 7/29/16.
 */
public class NasdaqWebParserTests {

    @Test
    public void QuoteSymbolePrice() {
        NasdaqWebParser parser = new NasdaqWebParser();
        double price = parser.QuoteSymbolePrice("jd");
        Assert.assertTrue(price > 0);
    }
}
