package src;

import src.PriceMonitor.PriceMonitor;
import src.PriceTrendProphet.TrendProphet;
import src.ResultPublisher.ResultPublisher;
import src.Utility.Constant;
import src.Utility.Email;
import src.Utility.Log;

import java.io.File;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by zhuoli on 6/23/16.
 */
public class Main {

    public static void main(String[] args) {

        try {
            new Main().start(args);
        } catch (Exception exc) {
            Log.PrintAndLog(exc.getMessage());
        }
    }


    // Entry of start method
    public void start(String[] args) throws Exception {

        Log.PrintAndLog("CurrencyProphet been launched. all rights reserved");

        File path = new File(Constant.DATA_ROOT);

        // Create Data directory if not exist
        if (!path.exists() || !path.isDirectory()) {
            path.mkdir();
        }

        // Initialize email recipient
        Scanner scanner = new Scanner(System.in);
        System.out.println("Input your recipient email:");
        String emailAddress = scanner.nextLine();
        System.out.println("Input your recipient password:");
        String pw = scanner.nextLine();

        Email user = Email.GetInstance(emailAddress, pw);
        try {
            user.Authenticate();
        } catch (Exception exc) {
            System.out.println("Error on authenticating email recipient" + exc.getMessage());
        }

        Log.PrintAndLog("Email authenticate succeed");
        Log.PrintAndLog("Monitor started...");

        // Task executor
        ExecutorService taskExecutor = Executors.newFixedThreadPool(3);

        // Submit Price monitor task
        taskExecutor.submit(() -> {
            new PriceMonitor().Start();
        });

        // Submit Price Prophet task
        taskExecutor.submit(() -> {
            new TrendProphet().Start();
        });

        // Submit analysis result publisher task
        taskExecutor.submit(() -> {
            new ResultPublisher().Start();
        });

        System.out.println("Main thread is waiting for child threads...");
        taskExecutor.wait();
    }
}
