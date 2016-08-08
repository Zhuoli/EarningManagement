package PriceMonitor;

import PriceMonitor.stock.NasdaqParser.NasdaqWebParser;
import PriceMonitor.stock.StockItem;

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

    // Register symbols to price monitor
    public static void RegisterStockSymboles(String... symbols) {
        // Lock the map for multi thread safe

        System.out.println("RegisterStockSymboles acquired lock: stockPriceMap");
        synchronized (PriceMonitor.stockPriceMap) {
            for (String symbol : symbols) {
                if (PriceMonitor.stockPriceMap.containsKey(symbol))
                    continue;
                PriceMonitor.stockPriceMap.put(symbol, new StockItem(symbol));
            }
        }
        System.out.println("RegisterStockSymboles released lock: stockPriceMap");
    }

    /**
     * Independent thread periodically update stock price
     *
     * @throws InterruptedException
     */
    public void Start() throws InterruptedException {

        NasdaqWebParser parser = new NasdaqWebParser();

        boolean shouldContinue = true;
        while (shouldContinue) {
            // Lock the map for multi thread safe
            //System.out.println("PriceMonitor acquired lock: stockPriceMap");
            synchronized (PriceMonitor.stockPriceMap) {
                for (String symbol : PriceMonitor.stockPriceMap.keySet()) {

                    try {
                        double stockPrice = parser.QuoteSymbolePrice(symbol);
                        String line = String.format("Symbol: %1$-8s Price: %2$.2f", symbol, stockPrice);
                        System.out.println(line);
                        StockItem stockItem = PriceMonitor.stockPriceMap.get(symbol);
                        stockItem.Price = stockPrice;
                        stockItem.LastUpdateTime = LocalDateTime.now();

                        // Sleep a while to avoid access limit
                        Thread.sleep(500);
                    } catch (Exception exc) {
                        String line = String.format("Symbol: %1$-8s Price: UNKNOWN", symbol);
                        System.err.println(line);
                        System.err.print(exc.getMessage());
                    }
                }
            }

            // Sleep a whole
            Thread.sleep(PriceMonitor.SCAN_PERIOD);
        }
    }
}
