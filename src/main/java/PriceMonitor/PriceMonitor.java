package PriceMonitor;

import DataManager.DataManager;
import JooqMap.tables.Sharedstockitems;
import JooqMap.tables.records.SharedstockitemsRecord;
import PriceMonitor.stock.NasdaqParser.NasdaqWebParser;
import PriceMonitor.stock.StockItem;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    public static void RegisterStockSymboles(SharedstockitemsRecord boughtStockItem) {
        // Lock the map for multi thread safe

        try {
            synchronized (PriceMonitor.stockPriceMap) {
                String symbol = boughtStockItem.getSymbol();
                double averageCost = boughtStockItem.getSharedaveragecost();
                int number = boughtStockItem.getShares();
                double targetPriceNasdaq = 0;
                try {
                    targetPriceNasdaq = boughtStockItem.getTargetprice();
                } catch (Exception e) {

                }

                // Update stock mapper
                if (PriceMonitor.stockPriceMap.containsKey(symbol)) {
                    StockItem item = PriceMonitor.stockPriceMap.get(boughtStockItem.get(Sharedstockitems.SHAREDSTOCKITEMS.SYMBOL));
                    item.AverageCost = averageCost;
                    item.Shares = number;
                } else {
                    PriceMonitor.stockPriceMap.put(symbol, new StockItem(symbol, averageCost, number, targetPriceNasdaq));
                }
            }

        } catch (Exception exc) {
            Logger.getGlobal().log(Level.SEVERE, "Failure to register stock symbol " + boughtStockItem.toString(), exc);
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

        try {

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

            Logger.getGlobal().severe("PriceMonitor dropped the infinite loop");
        } catch (Exception exc) {
            Logger.getGlobal().log(Level.SEVERE, "PriceMonitor crashed.", exc);
        }
    }
}
