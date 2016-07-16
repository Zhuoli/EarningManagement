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
import java.util.Arrays;
import java.util.LinkedList;

/**
 * Created by zhuoli on 6/25/16.
 */
public class DataManager {

    static int count = 0;

    Path path = null;

    long lastModifiedDateTime = 0;

    StockItem[] stockItems;

    /**
     * Constructor, create DATA_ROOT directory
     */
    public DataManager() throws IOException {
        this.InitializeStockCSVFile();
    }

    // Start thread
    public void Start() {
        try {
            while (true) {
                Thread.sleep(3 * 1000);
                System.out.println("Hello DataManager is running: " + DataManager.count++);

                // Update Stock Items if file size has changed
                if (this.lastModifiedDateTime == 0 || this.path.toFile().lastModified() != this.lastModifiedDateTime) {
                    this.lastModifiedDateTime = this.path.toFile().lastModified();
                    this.stockItems = this.ReadStockCSVFile();
                }
            }
        } catch (Exception exc) {
            Log.PrintAndLog("Price Prophet thread Interrupted: " + exc.getMessage());
        }
    }

    public String[] GetStockSymbolsInHand() {
        return Arrays.stream(stockItems).map(p -> p.Symbol).distinct().toArray(String[]::new);
    }

    /**
     * Initialize csv file if not exist.
     * Header format:
     * Stock Symbol | Bought Price | Bought Number
     */
    public void InitializeStockCSVFile() throws IOException {

        this.path = Paths.get(Constant.DATA_ROOT, "transactionRecords.csv").toAbsolutePath();

        File dir = new File(Constant.DATA_ROOT);

        // Create Data directory if not exist
        if (!dir.exists() || !dir.isDirectory()) {
            dir.mkdir();
        }

        // Create file if not exist
        if (!Files.exists(this.path)) {
            Files.createFile(this.path);
        }

        // Set the last modified date of the csv file
        this.lastModifiedDateTime = this.path.toFile().lastModified();
        this.stockItems = this.ReadStockCSVFile();

        System.out.println("The data file stored at : " + dir.getAbsolutePath());
    }

    /**
     * Read stocks from CSV file.
     *
     * @return stock item array.
     * @throws IOException
     */
    public StockItem[] ReadStockCSVFile() throws IOException {

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

        FileReader in = new FileReader(this.path.toFile());
        CSVParser records = CSVFormat.EXCEL.withFirstRecordAsHeader().withDelimiter('\t').parse(in);
        in.close();
        return records.getHeaderMap().keySet().toArray(new String[0]);
    }
}
