package PriceMonitor;

import JooqORM.tables.Stock;
import JooqORM.tables.records.StockRecord;
import PriceMonitor.stock.NasdaqParser.NasdaqWebParser;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
     *  Register symbols to price monitor
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
                    StockRecord item = PriceMonitor.stockPriceMap.get(boughtStockItem.get(Stock.STOCK.SYMBOL));
                    item.setSharedAverageCost(averageCost);
                    item.setShares(number);
                } else {
                    PriceMonitor.stockPriceMap.put(
                            symbol,
                            new StockRecord(
                                    symbol,
                                    Double.valueOf(0),
                                    Timestamp.valueOf(LocalDateTime.MIN),
                                    Timestamp.valueOf(LocalDateTime.MIN),
                                    averageCost,
                                    number,
                                    targetPriceNasdaq,
                                    Timestamp.valueOf(LocalDateTime.now())));
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
                for (StockRecord stockItem : PriceMonitor.stockPriceMap.values()) {

                    try {
                        stockItem.setCurrentPrice(parser.QuoteSymbolePrice(stockItem.getSymbol()));

                        stockItem.setCurrentPriceLatestUpdateTime(Timestamp.valueOf(LocalDateTime.now()));

                        // Sleep a while to avoid access limit
                        Thread.sleep(500);
                    } catch (Exception exc) {
                        String line = String.format("Symbol: %1$-8s Price: UNKNOWN", stockItem.getSymbol());
                        System.err.println(line);
                        System.err.print(exc.getMessage());
                    }

                    Optional<LocalDate> localDate = parser.QupteEarningReportDate(stockItem.getSymbol());

                    // Update earning report date
                    if (localDate.isPresent()) {
                        stockItem.setReportDate(Timestamp.valueOf(localDate.get().atStartOfDay()));
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
