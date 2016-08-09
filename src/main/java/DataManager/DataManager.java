package DataManager;

import Utility.Constant;
import Utility.Log;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by zhuoli on 6/25/16.
 */
public class DataManager {

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
            Log.PrintAndLog("Price Prophet thread Interrupted: " + exc.getMessage());
        }
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

        System.out.println("The data file stored at : " + dir.getAbsolutePath());
    }

    /**
     * Read stocks from CSV file.
     *
     * @return stock item array.
     * @throws IOException
     */
    public List<JSONObject> ReadStockCSVFile() throws IOException {

        LinkedList<JSONObject> jsonObjectList = new LinkedList<>();

        try (BufferedReader inReader = new BufferedReader(new FileReader(this.path.toFile()))) {
            CSVParser records = CSVFormat.EXCEL.withFirstRecordAsHeader().withDelimiter('\t').parse(inReader);
            this.Helper(jsonObjectList, records);

        } catch (Exception e) {
            System.err.println(e);
        }
        return jsonObjectList;
    }


    private void Helper(LinkedList<JSONObject> jsonObjectList, CSVParser records) {
        for (CSVRecord record : records) {
            String symbol = record.get("Symbol");
            double price = Double.parseDouble(record.get("Price"));
            int number = Integer.parseInt(record.get("Number"));
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("Symbol", symbol).put("Price", price).put("Number", number);
            jsonObjectList.add(jsonObject);
        }
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
