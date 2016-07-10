package src;

import com.joanzapata.utils.Strings;
import src.DataManager.DataManager;
import src.PriceMonitor.PriceMonitor;
import src.ResultPublisher.EmailManager.EmailManager;
import src.ResultPublisher.EmailManager.MonitorEmail;
import src.ResultPublisher.ResultPublisher;
import src.Utility.Email;
import src.Utility.Log;

import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by zhuoli on 6/23/16.
 */
public class Main {

    public static void main(String[] args) {

//        try {
//            new Main().start(args);
//        } catch (Exception exc) {
//            Log.PrintAndLog("Unexpected exception: " + exc.getMessage() + "\n" + exc.getStackTrace());
//        }

        try {
            EmailManager emailManager = new EmailManager();
            MonitorEmail[] newReceivedEmails = emailManager.Receive("inbox", "robotonyszu@gmail.com");
            for (MonitorEmail email : newReceivedEmails)
                System.out.println(Strings.format("Subject: {subject};\tText: {text}").with("subject", email.Subject).with("text", email.Content).build());
            emailManager.Send("robotonyszu@gmail.com", "hi this is subject", "I received " + newReceivedEmails.length + " unread emails.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // Entry of start method
    public void start(String[] args) throws Exception {

        Log.PrintAndLog("CurrencyProphet been launched. all rights reserved");

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

        DataManager dataManager = new DataManager();

//        // Submit data manager
//        taskExecutor.submit(() -> {
//            dataManager.Start();
//        });

        // Submit Price monitor task
        taskExecutor.submit(() -> {
            new PriceMonitor().Start();
        });

//        // Submit Price Prophet task
//        taskExecutor.submit(() -> {
//            new TrendProphet().Start();
//        });

        // Submit analysis result publisher task
        taskExecutor.submit(() -> {
            new ResultPublisher().Start();
        });

        System.out.println("Main thread is waiting for child threads...");
        taskExecutor.wait();
    }
}
