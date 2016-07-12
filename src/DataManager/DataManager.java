package src.DataManager;

import src.Utility.Constant;
import src.Utility.Log;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by zhuoli on 6/25/16.
 */
public class DataManager {

    static int count = 0;

    public DataManager() {

        File dir = new File(Constant.DATA_ROOT);

        // Create Data directory if not exist
        if (!dir.exists() || !dir.isDirectory()) {
            dir.mkdir();
        }

        System.out.println("The data file stored at : " + dir.getAbsolutePath());

    }

    public void Start() {
        try {
            while (true) {
                Thread.sleep(3 * 1000);
                System.out.println("Hello DataManager is running: " + DataManager.count++);
            }
        } catch (InterruptedException exc) {
            Log.PrintAndLog("Price Prophet thread Interrupted: " + exc.getMessage());
        }
    }

    public void InitializeStockXMLFile() {
        Path path = Paths.get(Constant.DATA_ROOT, "transactionRecords.xml");

        // Create file if not exist
        if (!Files.exists(path)) {
            try {
                Files.createFile(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
