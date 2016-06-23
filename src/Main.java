package src;

import src.Utility.Constant;
import src.Utility.Email;
import src.Utility.Log;

import java.io.File;
import java.util.Scanner;

/**
 * Created by zhuoli on 6/23/16.
 */
public class Main {

    public static void main(String[] args) {
        Log.PrintAndLog("Hello world");

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

        Email recipient = new Email();
        try {
            recipient.Authenticate(emailAddress, pw);
        } catch (Exception exc) {
            System.out.println("Error on authenticating email recipient" + exc.getMessage());
        }

        Log.PrintAndLog("Email authenticate succeed");
        Log.PrintAndLog("Monitor started...");
    }
}
