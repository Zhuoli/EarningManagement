package PriceMonitor;

import DataManager.StockRecord;
import PriceMonitor.stock.NasdaqParser.NasdaqWebParser;

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
     * Register symbols to price monitor or update the average cost and number and targetPrice of existing stock.
     */
    public static void RegisterStockSymboles(StockRecord boughtStockItem) {

        // Lock the map for multi thread safe
        try {
            synchronized (PriceMonitor.stockPriceMap) {
                String symbol = boughtStockItem.getSymbol();
                double averageCost = boughtStockItem.getSharedAverageCost();
                int number = boughtStockItem.getShares();
                double targetPriceNasdaq = 0;
                try {
                    targetPriceNasdaq = boughtStockItem.getTargetPrice();
                } catch (Exception e) {

                }

                // Update stock mapper
                if (PriceMonitor.stockPriceMap.containsKey(symbol)) {
                    StockRecord item = PriceMonitor.stockPriceMap.get(symbol);
                    item.setSharedAverageCost(averageCost);
                    item.setShares(number);
                } else {
                    PriceMonitor.stockPriceMap.put(
                            symbol,
                            StockRecord.builder().symbol(symbol).timestamp(new Date()).sharedAverageCost(averageCost).shares(number).targetPrice(targetPriceNasdaq).build());
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
            Logger.getGlobal().log(Level.SEVERE, "PriceMonitor crashed.", exc);
        }
    }

    private void task(NasdaqWebParser nasdaqWebParser) throws Exception {
        for (StockRecord stockItem : PriceMonitor.stockPriceMap.values()) {

            try {
                stockItem.setCurrentPrice(nasdaqWebParser.QuoteSymbolePrice(stockItem.getSymbol()));

                stockItem.setCurrentPriceLatestUpdateTime(new Date());

                // Sleep a while to avoid access limit
                Thread.sleep(500);
            } catch (Exception exc) {
                String line = String.format("Symbol: %1$-8s Price: UNKNOWN", stockItem.getSymbol());
                System.err.println(line);
                System.err.print(exc.getMessage());
            }

            Optional<LocalDate> localDate = nasdaqWebParser.QupteEarningReportDate(stockItem.getSymbol());

            // Update earning report date
            if (localDate.isPresent()) {
                stockItem.setReportDate(new Date());
            }
        }

    }
}
