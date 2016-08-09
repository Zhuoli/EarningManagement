import DataManager.DataManager;
import PriceMonitor.PriceMonitor;
import PriceMonitor.stock.StockItem;
import ResultPublisher.ResultPublisher;
import Utility.Log;

import java.util.Arrays;
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

        PriceMonitor priceMonitor = new PriceMonitor();

        // Initialize data manager and StockRegister method
        DataManager dataManager = new DataManager(PriceMonitor::RegisterStockSymboles);

        // Submit data manager
        taskExecutor.submit(() -> {
            publisher.Start();
        });

        // Submit data manager
        taskExecutor.submit(() -> {
            dataManager.Start();
        });

        // Submit Price monitor task
        taskExecutor.submit(() -> {
            try {
                priceMonitor.Start();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        while (true) {

            // Get stock prices for each data item
            StockItem[] stockItems = PriceMonitor.GetStocks();

            // Skip if items are null or empty
            if (stockItems == null || stockItems.length == 0) {
                System.out.println("Items are null or empty, sleep a while...");
                Thread.sleep(10 * 1000);
                continue;
            }

            // Buying value
            double baseValue = Arrays.stream(stockItems).map(item -> item.BuyingPrice * item.Number).reduce((a, b) -> a + b).get();
            double currentValue = Arrays.stream(stockItems).map(item -> item.Price * item.Number).reduce((a, b) -> a + b).get();
            StringBuilder sb = new StringBuilder("**************************************************************" + System.lineSeparator());
            sb.append(String.format("Buying price: %.2f" + System.lineSeparator(), baseValue));
            sb.append(String.format("Current value: %.2f" + System.lineSeparator(), currentValue));
            sb.append("**************************************************************" + System.lineSeparator());
            System.out.println(sb.toString());
            Thread.sleep(10 * 1000);
        }
    }
}
