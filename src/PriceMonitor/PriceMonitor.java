package src.PriceMonitor;

import src.PriceMonitor.CryptoCurrency.CryptoCurrencyPriceMonitor;
import src.PriceMonitor.stock.StockItem;
import src.PriceMonitor.stock.StockPriceMonitor;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhuoli on 7/4/16.
 */
public class PriceMonitor {

    // Scan period 10 second
    static final int SCAN_PERIOD = 10 * 1000;

    public static Map<String, StockItem> stockPriceMap;

    public PriceMonitor() {
        PriceMonitor.stockPriceMap = Collections.synchronizedMap(new HashMap<>());
    }

    public static StockItem[] GetStocks() {
        return PriceMonitor.stockPriceMap.values().toArray(new StockItem[0]);
    }

    // Register
    public static void RegisterStockSymboles(String... symbols) {
        // Lock the map for multi thread safe
        synchronized (PriceMonitor.stockPriceMap) {
            for (String symbol : symbols) {
                if (PriceMonitor.stockPriceMap.containsKey(symbol))
                    continue;
                PriceMonitor.stockPriceMap.put(symbol, new StockItem(symbol));
            }
        }
    }

    public void Start() throws InterruptedException {
        CryptoCurrencyPriceMonitor cryptoMonitor = new CryptoCurrencyPriceMonitor();
        StockPriceMonitor stockMonitor = new StockPriceMonitor();

        boolean shouldContinue = true;
        while (shouldContinue) {

            // Lock the map for multi thread safe
            synchronized (PriceMonitor.stockPriceMap) {
                for (String symbol : PriceMonitor.stockPriceMap.keySet()) {
                    double stockPrice = stockMonitor.GetPrice(symbol);
                    StockItem stockItem = PriceMonitor.stockPriceMap.get(symbol);
                    stockItem.Price = stockPrice;
                    stockItem.LastUpdateTime = LocalDateTime.now();
                }

                // Sleep a whole
                Thread.sleep(PriceMonitor.SCAN_PERIOD);
            }
        }
    }
}
