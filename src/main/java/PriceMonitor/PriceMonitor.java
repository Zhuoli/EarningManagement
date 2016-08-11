package PriceMonitor;

import DataManager.DataManager;
import PriceMonitor.stock.NasdaqParser.NasdaqWebParser;
import PriceMonitor.stock.StockItem;
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

    /**
     * Key: Symbol
     * Value: StockItem
     */
    public static Map<String, StockItem> stockPriceMap;


    public PriceMonitor() {
        PriceMonitor.stockPriceMap = Collections.synchronizedMap(new HashMap<>());
    }

    /**
     * Get stock items.
     *
     * @return
     */
    public static StockItem[] GetStocks() {
        return PriceMonitor.stockPriceMap.values().toArray(new StockItem[0]);
    }

    // Register symbols to price monitor
    public static void RegisterStockSymboles(JSONObject boughtStockItem) {
        // Lock the map for multi thread safe

        synchronized (PriceMonitor.stockPriceMap) {
            String symbol = boughtStockItem.getString(DataManager.SYMBOL);
            double price = boughtStockItem.getDouble(DataManager.PRICE);
            int number = boughtStockItem.getInt(DataManager.SHARES);
            if (PriceMonitor.stockPriceMap.containsKey(boughtStockItem.getString(DataManager.SYMBOL)))
                return;
            PriceMonitor.stockPriceMap.put(boughtStockItem.getString(DataManager.SYMBOL), new StockItem(symbol, price, number));
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
                for (StockItem stockItem : PriceMonitor.stockPriceMap.values()) {

                    try {
                        stockItem.Price = parser.QuoteSymbolePrice(stockItem.Symbol);

                        stockItem.LastUpdateTime = LocalDateTime.now();

                        // Sleep a while to avoid access limit
                        Thread.sleep(500);
                    } catch (Exception exc) {
                        String line = String.format("Symbol: %1$-8s Price: UNKNOWN", stockItem.Symbol);
                        System.err.println(line);
                        System.err.print(exc.getMessage());
                    }

                    Optional<LocalDate> localDate = parser.QupteEarningReportDate(stockItem.Symbol);

                    // Update earning report date
                    if (localDate.isPresent()) {
                        stockItem.setEarningReportDate(localDate.get());
                    }
                }
            }

            // Sleep a whole
            Thread.sleep(PriceMonitor.SCAN_PERIOD);
        }
    }
}
