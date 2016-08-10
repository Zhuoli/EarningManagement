import DataManager.DataManager;
import PriceMonitor.PriceMonitor;
import PriceMonitor.stock.StockItem;
import ResultPublisher.ResultPublisher;
import com.joanzapata.utils.Strings;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.*;

/**
 * Created by zhuoli on 6/23/16.
 */
public class StockMaster {

    public static void main(String[] args) {
        StockMaster.SetUp();
        try {
            new StockMaster().start(args);
            return;
        } catch (Exception exc) {
            Logger.getGlobal().log(Level.SEVERE, "Unexpected exception", exc);
            exc.printStackTrace();
        }
        System.exit(1);
    }

    /**
     * Sets up the envrionment.
     */
    public static void SetUp() {
        try {
            String filePath = Strings.format("./{classname}.log").with("classname", StockMaster.class.getName()).build();
            SimpleFormatter simpleFormatter = new SimpleFormatter();
            Handler fileHandler = new FileHandler(filePath);

            // Set file log format to plain text
            fileHandler.setFormatter(simpleFormatter);
            Logger.getGlobal().addHandler(fileHandler);
            fileHandler.setLevel(Level.ALL);
            System.out.println("Log setup succeed, log file stored at " + filePath);
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    /**
     * Entry of the StockMaster.
     *
     * @param args
     * @throws Exception
     */
    public void start(String[] args) throws Exception {

        // Log start time
        Logger.getGlobal().info("CurrencyProphet been launched. all rights reserved");

        // Task executor
        ExecutorService taskExecutor = Executors.newFixedThreadPool(3);

        ResultPublisher publisher = ResultPublisher.GetInstance().CollectInformationAndAuthenticate();

        System.out.println("Email authenticate succeed");
        System.out.println("Monitor started...");

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
                Logger.getGlobal().log(Level.SEVERE, "PriceMonitor thread has crashed for the reason" + e.getMessage(), e);
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
