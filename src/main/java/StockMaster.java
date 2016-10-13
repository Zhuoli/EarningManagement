import DataManager.DataManager;
import PriceMonitor.PriceMonitor;
import ResultPublisher.ResultPublisher;
import com.joanzapata.utils.Strings;

import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.*;

/**
 * Stock master.
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
     * Sets up the environment.
     */
    public static void SetUp() {
        try {

            // Log absolute path
            String filePath = Paths.get("").toAbsolutePath() + Strings.format("/{classname}.log").with("classname", StockMaster.class.getName()).build();

            // Log format
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
        System.exit(1);
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

        // Initialize result publisher and authenticate user information
        ResultPublisher publisher = ResultPublisher.GetInstance(PriceMonitor::GetStocks).CollectInformationAndAuthenticate();

        System.out.println("Email authenticate succeed");
        System.out.println("Monitor started...");

        // Initialize price monitor
        PriceMonitor priceMonitor = new PriceMonitor();

        // Initialize data manager and StockRegister method
        DataManager dataManager = DataManager.GetDataManager(PriceMonitor::RegisterStockSymboles, PriceMonitor::GetStocks);

        // Submit result publisher thread
        taskExecutor.submit(() -> {
            publisher.Start();
        });

        // Submit data manager thread
        taskExecutor.submit(() -> {
            dataManager.Start();
        });

        // Submit Price monitor thread
        taskExecutor.submit(() -> {
            try {
                priceMonitor.Start();
            } catch (InterruptedException e) {
                Logger.getGlobal().log(Level.SEVERE, "PriceMonitor thread has crashed for the reason" + e.getMessage(), e);
            }
        });

        // Initiates an orderly shutdown in which previously submitted tasks are executed, but no new tasks will be accepted. Invocation has no additional effect if already shut down.
        taskExecutor.shutdown();
        // Blocks until all tasks have completed execution after a shutdown request, or the timeout occurs, or the current thread is interrupted, whichever happens first.
        taskExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);

        // Log for Abnormal state
        Logger.getGlobal().severe("System shutdown");
    }
}
