
package src.DataManager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;

/**
 * Created by zhuoli on 7/12/16.
 */
public class DataManagerTest {

    // Crypto Currency Price Monitor
    private DataManager dataManager;

    @Before
    public void TestSetUp() throws IOException {
        this.dataManager = new DataManager();
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
        DataItem[] stockItems = this.dataManager.ReadStockCSVFile();

        Assert.assertNotNull(stockItems);
        Assert.assertTrue(stockItems.length > 0);
    }


    @Test
    /**
     * Test Read GetStockSymbolsInHand.
     */
    public void testGetStockSymbolsInHand() {
        String[] symbols = this.dataManager.GetStockSymbolsInHand();

        Assert.assertNotNull(symbols);
        Assert.assertTrue(symbols.length > 0);
    }
}
