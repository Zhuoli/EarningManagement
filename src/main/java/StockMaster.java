import DataManager.DataItem;
import DataManager.DataManager;
import PriceMonitor.PriceMonitor;
import PriceMonitor.stock.StockItem;
import ResultPublisher.ResultPublisher;
import Utility.Log;

import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by zhuoli on 6/23/16.
 */
public class StockMaster {
    public static void main(String[] args) {

        try {
            new StockMaster().start(args);
            return;
        } catch (Exception exc) {
            Log.Error("Unexpected exception: " + exc.toString());
            exc.printStackTrace();
        }
        System.exit(1);
    }


    // Entry of start method
    public void start(String[] args) throws Exception {

        Log.PrintAndLog("CurrencyProphet been launched. all rights reserved");

        // Task executor
        ExecutorService taskExecutor = Executors.newFixedThreadPool(3);

        ResultPublisher publisher = ResultPublisher.GetInstance().CollectInformationAndAuthenticate();

        Log.PrintAndLog("Email authenticate succeed");
        Log.PrintAndLog("Monitor started...");

        // Submit data manager
        taskExecutor.submit(() -> {
            publisher.Start();
        });

        DataManager dataManager = new DataManager();

        // Submit data manager
        taskExecutor.submit(() -> {
            dataManager.Start();
        });

        // Submit Price monitor task
        taskExecutor.submit(() -> {
            try {
                new PriceMonitor().Start();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

//        // Submit Price Prophet task
//        taskExecutor.submit(() -> {
//            new TrendProphet().Start();
//        });

        // Submit analysis result publisher task
//        taskExecutor.submit(() -> {
//            new ResultPublisher().Start();
//        });

        HashMap<String, StockItem> stockPriceMap = new HashMap<>();
        while (true) {

            // Keeps updating symbols from data manager
            String[] symbols = dataManager.GetStockSymbolsInHand();

            // Register new symbols to price monitor
            PriceMonitor.RegisterStockSymboles(symbols);

            // Get buying items
            DataItem[] dataItems = dataManager.GetDataItems();

            // Get stock prices for each data item
            StockItem[] stockItems = PriceMonitor.GetStocks();

            // Skip if items are null or empty
            if (dataItems == null || dataItems.length == 0 || stockItems == null || stockItems.length == 0) {
                System.out.println("Items are null or empty, sleep a while...");
                Thread.sleep(10 * 1000);
                continue;
            }

            // Update Symbol to StockItem hash map
            Arrays.stream(stockItems).forEach(p -> stockPriceMap.put(p.Symbol, p));

            // Buying value
            double baseValue = Arrays.stream(dataItems).map(item -> item.Price * item.Number).reduce((a, b) -> a + b).get();
            double currentValue = 0;
            for (DataItem item : dataItems) {
                if (stockPriceMap.containsKey(item.Symbol)) {
                    StockItem stockItem = stockPriceMap.get(item.Symbol);
                    currentValue += stockItem.Price * item.Number;
                } else {
                    System.out.println("Symbol price not found : " + item.Symbol);
                }
            }
            System.out.println(String.format("Buying price: %.2f", baseValue));
            System.out.println(String.format("Current value: %.2f", currentValue));
            Thread.sleep(10 * 1000);
        }
    }
}
