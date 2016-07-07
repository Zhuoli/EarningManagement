package src.PriceMonitor;

import com.joanzapata.utils.Strings;
import src.PriceMonitor.CryptoCurrency.CryptoCurrencyPriceMonitor;
import src.PriceMonitor.CryptoCurrency.CurrencyEnum;
import src.PriceMonitor.stock.StockPriceMonitor;

/**
 * Created by zhuoli on 7/4/16.
 */
public class PriceMonitor {

    // Scan period 10 second
    static final int SCAN_PERIOD = 10 * 1000;

    public void Start() {
        CryptoCurrencyPriceMonitor cryptoMonitor = new CryptoCurrencyPriceMonitor();
        StockPriceMonitor stockMonitor = new StockPriceMonitor();

        boolean shouldContinue = true;
        while (shouldContinue) {
            try {
                Thread.sleep(PriceMonitor.SCAN_PERIOD);
            } catch (InterruptedException e) {
                System.out.println(Strings.format("Interrupted exception: {msg}").with("msg", e.getMessage()));
            }
            cryptoMonitor.GetPriceForCurrency(CurrencyEnum.LiteCoin);
            stockMonitor.GetPrice("AAPL");
        }
    }
}
