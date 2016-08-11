
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
        this.dataManager = new DataManager(a -> a.get(DataManager.SYMBOL));
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
        Assert.assertEquals(headers[0], DataManager.SYMBOL);
        Assert.assertEquals(headers[1], DataManager.PRICE);
        Assert.assertEquals(headers[2], DataManager.SHARES);
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
