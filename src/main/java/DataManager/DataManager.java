package DataManager;

import Utility.Constant;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by zhuoli on 6/25/16.
 */
public class DataManager {

    static final public String SYMBOL = "Symbol";
    static final public String PRICE = "Price";
    static final public String SHARES = "Shares";
    protected Path path = null;
    Consumer<JSONObject> stockItemRegister;
    private long lastModifiedDateTime = 0;

    /**
     * Constructor, create DATA_ROOT directory
     */
    public DataManager(Consumer<JSONObject> stockItemRegister) throws IOException {
        this.stockItemRegister = stockItemRegister;
        this.InitializeStockCSVFile();
    }

    // Start thread
    public void Start() {
        try {
            while (true) {
                // Re-register stock Items if file size has changed
                if (this.lastModifiedDateTime == 0 || this.path.toFile().lastModified() != this.lastModifiedDateTime) {
                    this.lastModifiedDateTime = this.path.toFile().lastModified();
                    this.ReadStockCSVFile().stream().forEach(stockItem -> this.stockItemRegister.accept(stockItem));
                }
                Thread.sleep(3 * 1000);
            }
        } catch (Exception exc) {
            Logger.getGlobal().severe("Price Prophet thread Interrupted: " + exc.getMessage());
        }
    }

    /**
     * Initialize csv file if not exist.
     * Header format:
     * Stock Symbol | Bought Price | Bought Shares
     */
    public void InitializeStockCSVFile() throws IOException {

        this.path = Paths.get(Constant.DATA_ROOT, "transactionRecords.csv").toAbsolutePath();

        // Update csv location
        if (!Files.exists(this.path))
            this.path = Paths.get("transactionRecords.csv");

        // Create file if not exist
        if (!Files.exists(this.path)) {
            Files.createFile(this.path);
        }

        System.out.println("The transaction records file stored at : " + this.path.toUri());
    }

    /**
     * Read stocks from CSV file.
     * @return stock item array.
     * @throws IOException
     */
    public List<JSONObject> ReadStockCSVFile() throws IOException {

        LinkedList<JSONObject> jsonObjectList = new LinkedList<>();

        try (BufferedReader inReader = new BufferedReader(new FileReader(this.path.toFile()))) {
            CSVParser records = CSVFormat.EXCEL.withFirstRecordAsHeader().withDelimiter('\t').parse(inReader);
            for (CSVRecord record : records) {
                String symbol = record.get(DataManager.SYMBOL);
                double price = Double.parseDouble(record.get(DataManager.PRICE));
                int number = Integer.parseInt(record.get(DataManager.SHARES));
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(DataManager.SYMBOL, symbol).put(DataManager.PRICE, price).put(DataManager.SHARES, number);
                jsonObjectList.add(jsonObject);
            }

        } catch (Exception e) {
            Logger.getGlobal().log(Level.SEVERE, "Failed to read stock CSV file", e);
        }
        return jsonObjectList;
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
