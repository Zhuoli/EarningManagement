package src.PriceMonitor.stock;

import com.joanzapata.utils.Strings;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

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
    public void TestGetPrice() {
        double price = this.stockPriceMonitor.GetPrice("AAPL");

        // Apple price should greater than 1 dollar
        Assert.assertTrue(price > 1);
    }

    @Test
    public void TestCompanyLookUp() {
        String[] NFLXNames = this.stockPriceMonitor.CompanyLookUp("NFLX");
        String[] nonExistNames = this.stockPriceMonitor.CompanyLookUp("aaaa");

        Assert.assertEquals(NFLXNames.length, 1);
        Assert.assertEquals(NFLXNames[0], "Netflix Inc");

        Assert.assertEquals(nonExistNames.length, 0);
    }

    @Test
    public void TestGetCompanyName() {
        String soapString = "[{\"Symbol\":\"AAPL\",\"Name\":\"Apple Inc\",\"Exchange\":\"NASDAQ\"},{\"Symbol\":\"AVSPY\",\"Name\":\"AAPL ALPHA INDEX\",\"Exchange\":\"NASDAQ\"},{\"Symbol\":\"AIX\",\"Name\":\"NAS OMX Alpha   AAPL vs. SPY  Settle\",\"Exchange\":\"NASDAQ\"}]";
        String[] companyNames = this.stockPriceMonitor.GetCompanyName(soapString, "AAPL");
        System.out.println("Company name: " + Arrays.toString(companyNames));

        Assert.assertEquals(companyNames.length, 1);
        Assert.assertEquals(companyNames[0], "Apple Inc");
    }

    @Test
    /**
     * Test SOAP API of http://coinmarketcap-nexuist.rhcloud.com
     */
    public void TestGetHttpResponse() {
        String result = this.stockPriceMonitor.GetHttpResponse(this.stockPriceMonitor.ComposeQuoteUrl("AAPL"));

        System.out.println("StockQuote Response: " + result);
        Assert.assertNotNull(result);
    }

    @Test
    public void TestComposeUrl() {
        String url = this.stockPriceMonitor.ComposeQuoteUrl("AAPL");
        System.out.println(Strings.format("TestComposeUrl url: {url}").with("url", url));

        System.out.println(this.stockPriceMonitor.GetHttpResponse(url));
    }

    @Test
    /**
     * Test parsing Markitondemand SOAP String.
     */
    public void TestParseMarkitondemandPrice() {
        String soapString = "{\"Status\":\"SUCCESS\",\"Name\":\"Apple Inc\",\"Symbol\":\"AAPL\",\"LastPrice\":95.895,\"Change\":0.295000000000002,\"ChangePercent\":0.308577405857742,\"Timestamp\":\"Fri Jul 1 15:59:00 UTC-04:00 2016\",\"MSDate\":42552.6659722222,\"MarketCap\":525257670375,\"Volume\":2562784,\"ChangeYTD\":105.26,\"ChangePercentYTD\":-8.89701691050732,\"High\":96.46,\"Low\":95.33,\"Open\":95.48}";
        double price = this.stockPriceMonitor.ParseMarkitondemandPrice(soapString);
        Assert.assertTrue("Failed on parsing sample soap string price", price == 95.895);
    }
}
