package src.DataManager;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import src.Utility.Constant;
import src.Utility.Log;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;

/**
 * Created by zhuoli on 6/25/16.
 */
public class DataManager {

    static int count = 0;

    Path path = null;

    StockItem[] stockItems;

    /**
     * Constructor, create DATA_ROOT directory
     */
    public DataManager() {

        File dir = new File(Constant.DATA_ROOT);

        // Create Data directory if not exist
        if (!dir.exists() || !dir.isDirectory()) {
            dir.mkdir();
        }
        this.path = Paths.get(Constant.DATA_ROOT, "transactionRecords.csv").toAbsolutePath();
        System.out.println("The data file stored at : " + dir.getAbsolutePath());

    }

    // Start thread
    public void Start() {
        try {
            // Initialize stock items array
            this.stockItems = this.ReadStockCSVFile();
            while (true) {
                Thread.sleep(3 * 1000);
                System.out.println("Hello DataManager is running: " + DataManager.count++);
            }
        } catch (Exception exc) {
            Log.PrintAndLog("Price Prophet thread Interrupted: " + exc.getMessage());
        }
    }

    /**
     * Initialize csv file if not exist.
     * Header format:
     * Stock Symbol | Bought Price | Bought Number
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

    /**
     * Read stocks from CSV file.
     *
     * @return stock item array.
     * @throws IOException
     */
    public StockItem[] ReadStockCSVFile() throws IOException {
        this.InitializeStockCSVFile();

        LinkedList<StockItem> stockItems = new LinkedList<>();
        FileReader in = new FileReader(this.path.toFile());
        CSVParser records = CSVFormat.EXCEL.withFirstRecordAsHeader().withDelimiter('\t').parse(in);
        for (CSVRecord record : records) {
            String symbol = record.get("Symbol");
            double price = Double.parseDouble(record.get("Price"));
            int number = Integer.parseInt(record.get("Number"));
            stockItems.add(new StockItem(symbol, price, number));
        }
        in.close();
        return stockItems.toArray(new StockItem[0]);
    }

    /**
     * Get CSV headers.
     *
     * @return Header array.
     * @throws IOException
     */
    public String[] Getheaders() throws IOException {
        this.InitializeStockCSVFile();

        LinkedList<String> headers = new LinkedList<>();
        FileReader in = new FileReader(this.path.toFile());
        CSVParser records = CSVFormat.EXCEL.withFirstRecordAsHeader().withDelimiter('\t').parse(in);
        in.close();
        return records.getHeaderMap().keySet().toArray(new String[0]);
    }
}
