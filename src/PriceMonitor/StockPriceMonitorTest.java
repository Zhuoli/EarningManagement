package src.PriceMonitor;

import com.joanzapata.utils.Strings;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by zhuoli on 7/4/16.
 */
public class StockPriceMonitorTest {

    // Stock Price Monitor
    private StockPriceMonitor stockPriceMonitor;

    @Before
    public void TestSetUp() {
        this.stockPriceMonitor = new StockPriceMonitor();
    }


    @Test
    /**
     * Test SOAP API of http://coinmarketcap-nexuist.rhcloud.com
     */
    public void TestGetHttpResponse() {
        String result = this.stockPriceMonitor.GetHttpResponse(this.stockPriceMonitor.BASE_URL);

        System.out.println("StockQuote Response: " + result);
        Assert.assertNotNull(result);
    }

    @Test
    public void TestComposeUrl() {
        String url = this.stockPriceMonitor.ComposeUrl("AAPL");
        System.out.println(Strings.format("TestComposeUrl url: {url}").with("url", url));

        System.out.println(this.stockPriceMonitor.GetHttpResponse(url));
    }
}
