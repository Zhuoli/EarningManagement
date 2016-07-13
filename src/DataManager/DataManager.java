package src.DataManager;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import src.Utility.Constant;
import src.Utility.Log;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by zhuoli on 6/25/16.
 */
public class DataManager {

    static int count = 0;

    Path path = null;


    public DataManager() {

        File dir = new File(Constant.DATA_ROOT);

        // Create Data directory if not exist
        if (!dir.exists() || !dir.isDirectory()) {
            dir.mkdir();
        }
        this.path = Paths.get(Constant.DATA_ROOT, "transactionRecords.xml").toAbsolutePath();
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

    /**
     * Initialize csv file if not exist.
     */
    public void InitializeStockCSVFile() {

        // Create file if not exist
        if (!Files.exists(this.path)) {
            try {
                Files.createFile(this.path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void ReadStockCSVFile() throws IOException {
        this.InitializeStockCSVFile();

        FileReader in = new FileReader(this.path.toFile());
        Iterable<CSVRecord> records = CSVFormat.EXCEL.parse(in);
        for (CSVRecord record : records) {
            String lastName = record.get("Last Name");
            String firstName = record.get("First Name");
        }
    }
}
