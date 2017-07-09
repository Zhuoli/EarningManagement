package com.zhuoli.earning.PriceMonitor;

import com.zhuoli.earning.DataManager.StockRecord;
import com.zhuoli.earning.PriceMonitor.stock.NasdaqParser.NasdaqWebParser;

import java.time.LocalDate;
import java.util.*;
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
    public static Map<String, StockRecord> stockPriceMap;


    public PriceMonitor() {
        PriceMonitor.stockPriceMap = Collections.synchronizedMap(new HashMap<>());
    }

    /**
     * Get stock items.
     *
     * @return
     */
    public static StockRecord[] GetStocks() {
        return PriceMonitor.stockPriceMap.values().toArray(new StockRecord[0]);
    }

    /**
     * Register symbols to price monitor
     */
    public static void RegisterStockSymboles(StockRecord boughtStockItem) {
        // Lock the map for multi thread safe
        try {
                String symbol = boughtStockItem.getSymbol();
            if (!PriceMonitor.stockPriceMap.containsKey(symbol)) {
                PriceMonitor.stockPriceMap.put(symbol, boughtStockItem);
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

        NasdaqWebParser nasdaqWebParser = new NasdaqWebParser();

        boolean shouldContinue = true;

        try {

            int errorCount = 0;
            while (shouldContinue) {
                try {
                    this.task(nasdaqWebParser);
                    // Reset error count
                    errorCount = 0;
                } catch (Exception e) {
                    if (++errorCount == 3)
                        throw e;
                }

                // Sleep a whole
                Thread.sleep(PriceMonitor.SCAN_PERIOD);
            }

            Logger.getGlobal().severe("PriceMonitor dropped the infinite loop");
        } catch (Exception exc) {
            Logger.getGlobal().log(Level.SEVERE, "com.zhuoli.earning.PriceMonitor crashed.", exc);
        }
    }

    private void task(NasdaqWebParser nasdaqWebParser) throws Exception {
        for (StockRecord stockItem : PriceMonitor.stockPriceMap.values()) {

            try {
                double newPrice = nasdaqWebParser.QuoteSymbolePrice(stockItem.getSymbol());
                if (stockItem.getCurrentPrice() != newPrice) {
                    stockItem.setCurrentPrice(newPrice);
                    stockItem.setCurrentPriceLatestUpdateTime(new Date());
                    stockItem.setHasUpdate(true);
                }

                // Sleep a while to avoid access limit
                Thread.sleep(500);
            } catch (Exception exc) {
                String line = String.format("Symbol: %1$-8s Price: UNKNOWN", stockItem.getSymbol());
                System.err.println(line);
                System.err.print(exc.getMessage());
            }

            Optional<LocalDate> newReportDate = nasdaqWebParser.QupteEarningReportDate(stockItem.getSymbol());

            // Update earning report date
            if (newReportDate.isPresent()) {
                if (stockItem.getReportDate() == null || (newReportDate.get().isAfter(stockItem.getReportDate()))) {
                    stockItem.setReportDate(newReportDate.get());
                    stockItem.setHasUpdate(true);
                }
            }
        }

    }
}
