package Utility;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

/**
 * Created by zhuoli on 6/23/16.
 */
public class Log {

    final static String LogFile = "StockMonitor.log";

    public static void PrintAndLog(String message) {
        System.out.println(message);
        Log.Information(message);
    }

    public static void Error(String message) {
        Log.BaseLog("Error", message);
    }

    public static void Information(String message) {
        Log.BaseLog("Information", message);
    }

    public static void BaseLog(String type, String message) {
        try {
            Log.CreateLogFileIfNotExist();
            //the true will append the new data
            try (FileWriter fw = new FileWriter(Log.LogFile, true)) {
                Date d = new Date();
                fw.write(d.toString() + " : " + type + " : " + message + "\n"); //appends the string to the file
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void CreateLogFileIfNotExist() throws IOException {
        File logFile = new File(Log.LogFile);

        if (!logFile.exists())
            logFile.createNewFile();
    }
}
