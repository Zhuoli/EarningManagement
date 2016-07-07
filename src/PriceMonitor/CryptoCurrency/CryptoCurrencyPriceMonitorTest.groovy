package src.PriceMonitor.CryptoCurrency

import org.junit.Assert
import org.junit.Before
import org.junit.Test

/**
 * Created by zhuoli on 6/29/16.
 */
public class CryptoCurrencyPriceMonitorTest {

    // Crypto Currency Price Monitor
    private CryptoCurrencyPriceMonitor cryptoCurrencyPriceMonitor;

    @Before
    public void TestSetUp() {
        this.cryptoCurrencyPriceMonitor = new CryptoCurrencyPriceMonitor();
    }


    @Test
    /**
     * Test SOAP API of http://coinmarketcap-nexuist.rhcloud.com
     */
    public void testGetSOAPString() throws Exception {

        // Get BitCoin price
        double bitCoinPrice = this.cryptoCurrencyPriceMonitor.GetPriceForCurrency(CurrencyEnum.BitCoin);
        double liteCoinPrice = this.cryptoCurrencyPriceMonitor.GetPriceForCurrency(CurrencyEnum.LiteCoin);
        double dogeCoinPrice = this.cryptoCurrencyPriceMonitor.GetPriceForCurrency(CurrencyEnum.DogeCoin);

        System.out.println("BitCoin Price: " + bitCoinPrice);
        System.out.println("LiteCoin price: " + liteCoinPrice);
        System.out.println("DogeCoin price: " + dogeCoinPrice);

        Assert.assertTrue(bitCoinPrice > 0);
        Assert.assertTrue(liteCoinPrice > 0);
        Assert.assertTrue(dogeCoinPrice > 0);
    }

    @Test
    /**
     * Test SOAP String parsing
     */
    public void testGetPriceFromCoinmarketcapSoapString() throws Exception {
        String soapString = "{\"symbol\":\"btc\",\"position\":\"1\",\"name\":\"Bitcoin\",\"market_cap\":{\"usd\":10027475766.5,\"eur\":9022221320.908377,\"cny\":66609984565.49058,\"gbp\":7478220684.810005,\"cad\":12991998702.108059,\"rub\":640046658667.9008,\"hkd\":77800948345.17758,\"jpy\":1029790676044.674,\"aud\":13465145146.150362,\"btc\":\"15715150.0\"},\"price\":{\"usd\":638.077,\"eur\":574.10978075,\"cny\":4238.584077519,\"gbp\":475.86059852100004,\"cad\":826.71808428,\"rub\":40728.001875329996,\"hkd\":4950.697151829,\"jpy\":65528.529861300005,\"aud\":856.8257475249999,\"btc\":\"1.0\"}, \"supply\":\"15715150\",\"volume\":{\"usd\":119284000,\"eur\":107325779.00000001,\"cny\":792373433.148,\"gbp\":88958786.532,\"cad\":154549121.76,\"rub\":7613813028.36,\"hkd\":925497955.6680001,\"jpy\":12250097019.6,\"aud\":160177537.29999998,\"btc\":\"186909.0\"}, \"change\":\"1.06\",\"timestamp\":\"1467265624.828\" }";

        int result = this.cryptoCurrencyPriceMonitor.GetPriceString(soapString);
        System.out.println("BitCoin USD: " + result);

        Assert.assertNotNull("Test result on GetPrice" + result);
    }
}
