package src.DataManager;

import src.Utility.Constant;
import src.Utility.Log;

import java.io.File;

/**
 * Created by zhuoli on 6/25/16.
 */
public class DataManager {

    static int count = 0;

    public DataManager() {

        File path = new File(Constant.DATA_ROOT);

        // Create Data directory if not exist
        if (!path.exists() || !path.isDirectory()) {
            path.mkdir();
        }

        System.out.println("The data file stored at : " + path.getAbsolutePath());

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
}
