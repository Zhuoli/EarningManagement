package src;

import src.DataManager.DataItem;
import src.DataManager.DataManager;
import src.PriceMonitor.PriceMonitor;
import src.PriceMonitor.stock.StockItem;
import src.Utility.Log;

import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by zhuoli on 6/23/16.
 */
public class Main {
    public static void main(String[] args) {

        try {
            new Main().start(args);
            return;
        } catch (Exception exc) {
            Log.PrintAndLog("Unexpected exception: " + exc.toString());
            exc.printStackTrace();
        }
        System.exit(1);
    }


    // Entry of start method
    public void start(String[] args) throws Exception {

        Log.PrintAndLog("CurrencyProphet been launched. all rights reserved");

//        // Initialize email recipient
//        Scanner scanner = new Scanner(System.in);
//        System.out.println("Input your recipient email:");
//        String emailAddress = scanner.nextLine();
//        System.out.println("Input your recipient password:");
//        String pw = scanner.nextLine();
//
//        Email user = Email.GetInstance(emailAddress, pw);
//        try {
//            user.Authenticate();
//        } catch (Exception exc) {
//            System.out.println("Error on authenticating email recipient" + exc.getMessage());
//        }

        Log.PrintAndLog("Email authenticate succeed");
        Log.PrintAndLog("Monitor started...");


        // Task executor
        ExecutorService taskExecutor = Executors.newFixedThreadPool(3);

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
            String[] symbols = dataManager.GetStockSymbolsInHand();
            PriceMonitor.RegisterStockSymboles(symbols);
            DataItem[] dataItems = dataManager.GetDataItems();
            StockItem[] stockItems = PriceMonitor.GetStocks();

            Arrays.stream(stockItems).forEach(p -> stockPriceMap.put(p.Symbol, p));

            double baseValue = Arrays.stream(dataItems).map(item -> item.Price * item.Number).reduce((a, b) -> a + b).get();
            double currentValue = 0;
            for (DataItem item : dataItems) {
                if (stockPriceMap.containsKey(item.Symbol)) {
                    StockItem stockItem = stockPriceMap.get(item.Symbol);
                    currentValue += stockItem.Price * item.Number;
                } else {
                    System.out.println("Symbole price not found : " + item.Symbol);
                }
            }
            System.out.println("Buying price: " + baseValue);
            System.out.println("Current value: " + currentValue);
            Thread.sleep(10 * 1000);
        }
    }
}
