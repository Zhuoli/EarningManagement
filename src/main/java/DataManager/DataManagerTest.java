
package DataManager;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

/**
 * Created by zhuoli on 7/12/16.
 */
public class DataManagerTest {

    // Crypto Currency Price Monitor
    private DataManager dataManager;

    @Before
    public void TestSetUp() throws IOException {
        this.dataManager = new DataManager(a -> a.get("Symbol"));
    }

    @Test
    public void testInitializeStockCSVFile() throws IOException {
        this.dataManager.InitializeStockCSVFile();

        Assert.assertTrue(Files.exists(this.dataManager.path));
    }


    @Test
    /**
     * Test Get csv file header.
     */
    public void testGetHeaders() throws Exception {
        String[] headers = this.dataManager.Getheaders();

        Assert.assertTrue(headers.length == 3);
        Assert.assertEquals(headers[0], "Symbol");
        Assert.assertEquals(headers[1], "Price");
        Assert.assertEquals(headers[2], "Number");
    }

    @Test
    /**
     * Test Read StockItems.
     */
    public void testReadStockCSVFile() throws IOException {
        List<JSONObject> stockItems = this.dataManager.ReadStockCSVFile();

        Assert.assertNotNull(stockItems);
        Assert.assertTrue(stockItems.size() > 0);
    }
}
