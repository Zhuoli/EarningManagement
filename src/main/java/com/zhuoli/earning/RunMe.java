package com.zhuoli.earning;

import com.zhuoli.earning.PriceMonitor.*;
import com.zhuoli.earning.ResultPublisher.*;
import com.joanzapata.utils.Strings;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.*;
import org.apache.commons.cli.*;
import com.zhuoli.earning.DataManager.*;

/**
 * Stock master.
 * Created by zhuoli on 6/23/16.
 */
public class RunMe {
    public static final String DISABLE_EMAIL = "d";
    public static CommandLine CMD;

    public static void main(String[] args) {
        RunMe runMe = new RunMe();
        if (!runMe.SetUp(args))
            return;
        new RunMe().start();
    }

    /**
     * Sets up the environment.
     */
    public  boolean SetUp(String[] args) {
        // create Options object
        Options options = new Options();

        // add t option
        options.addOption(RunMe.DISABLE_EMAIL, false, "Disable email or not");
        CommandLineParser parser = new DefaultParser();

        try {
            RunMe.CMD = parser.parse(options, args);
        } catch (ParseException exc) {
            System.err.println("Arguments parse exception: " + exc.getMessage());
        }

        try {

            // Log absolute path
            String filePath = Paths.get("").toAbsolutePath() + Strings.format("/{classname}.log").with("classname", RunMe.class.getName()).build();

            // Log format
            SimpleFormatter simpleFormatter = new SimpleFormatter();
            Handler fileHandler = new FileHandler(filePath);

            // Set file log format to plain text
            fileHandler.setFormatter(simpleFormatter);
            Logger.getGlobal().addHandler(fileHandler);
            fileHandler.setLevel(Level.ALL);
            System.out.println("Log setup succeed, log file stored at : '" + filePath + "'");
        } catch (Exception exc) {
            exc.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Entry of the RunMe.
     *
     */
    public boolean start(){

        // Log start time
        Logger.getGlobal().info("CurrencyProphet been launched. all rights reserved");

        // Task executor
        ExecutorService taskExecutor = Executors.newFixedThreadPool(3);

        // Initialize result publisher and authenticate user information
        Optional<ResultPublisher> publisher = ResultPublisher.GetInstance().CollectInformationAndAuthenticate();

        if (!publisher.isPresent()){
            System.err.println("Authentication failed, system going exit...");
            return false;
        }

        System.out.println("Email authenticate succeed");
        System.out.println("Monitor started...");

        // Initialize price monitor
        PriceMonitor priceMonitor = new PriceMonitor();

        // Initialize data manager and StockRegister method
        MongoDBManager dataManager = new MongoDBManager();

        // Submit result publisher thread
        Future publisherTask = taskExecutor.submit(() -> {
            publisher.get().Start();
        });

        // Submit data manager thread
        Future dataManagerTask = taskExecutor.submit(() -> {
            dataManager.Start();
        });

        // Submit Price monitor thread
        Future priceMonitorTask = taskExecutor.submit(() -> {
            try {
                priceMonitor.Start();
            } catch (InterruptedException e) {
                Logger.getGlobal().log(Level.SEVERE, "com.zhuoli.earning.PriceMonitor thread has crashed for the reason" + e.getMessage(), e);
            }
        });

        // Initiates an orderly shutdown in which previously submitted tasks are executed, but no new tasks will be accepted. Invocation has no additional effect if already shut down.
        taskExecutor.shutdown();

        try {
            // Blocks until all tasks have completed execution after a shutdown request, or the timeout occurs, or the current thread is interrupted, whichever happens first.
            while (!taskExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                if (publisherTask.isCancelled() || publisherTask.isDone()) {

                    Logger.getGlobal().log(Level.SEVERE, "Publisher task exit abnormally.");
                    System.exit(1);
                }

                if (dataManagerTask.isCancelled() || dataManagerTask.isDone()) {

                    Logger.getGlobal().log(Level.SEVERE, "dataManagerTask task exit abnormally.");
                    System.exit(1);
                }
                if (priceMonitorTask.isCancelled() || priceMonitorTask.isDone()) {

                    Logger.getGlobal().log(Level.SEVERE, "priceMonitorTask task exit abnormally.");
                    System.exit(1);
                }
            }
        } catch (InterruptedException exc){
            exc.printStackTrace();
        }

        // Log for Abnormal state
        Logger.getGlobal().severe("System shutdown");
        return true;
    }
}
