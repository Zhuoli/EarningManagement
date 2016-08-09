package PriceMonitor;

import PriceMonitor.stock.NasdaqParser.NasdaqWebParser;
import PriceMonitor.stock.StockItem;
import ResultPublisher.ResultPublisher;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
    public static void RegisterStockSymboles(JSONObject boughtStockItem) {
        // Lock the map for multi thread safe

        synchronized (PriceMonitor.stockPriceMap) {
            String symbol = boughtStockItem.getString("Symbol");
            double price = boughtStockItem.getDouble("Price");
            int number = boughtStockItem.getInt("Number");
            if (PriceMonitor.stockPriceMap.containsKey(boughtStockItem.getString("Symbol")))
                return;
            PriceMonitor.stockPriceMap.put(boughtStockItem.getString("Symbol"), new StockItem(symbol, price, number));
        }
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

                    Optional<LocalDate> localDate = parser.QupteEarningReportDate(symbol);

                    // Register Earning Report to Result publisher
                    if (localDate.isPresent()) {
                        ResultPublisher.RegisterMessage(String.format("%1$-8s quarterly earning report date: %2$s ", symbol, localDate.get()));
                    }
                }
            }

            // Sleep a whole
            Thread.sleep(PriceMonitor.SCAN_PERIOD);
        }
    }
}
