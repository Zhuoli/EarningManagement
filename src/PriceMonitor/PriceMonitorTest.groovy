package src.PriceMonitor

import org.junit.Before
import org.junit.Test

/**
 * Created by zhuoli on 6/29/16.
 */
public class PriceMonitorTest {

    private PriceMonitor priceMonitor;

    @Before
    public void TestSetUp() {
        this.priceMonitor = new PriceMonitor();
    }

    @Test
    public void GetPriceStringTest() {
        this.priceMonitor.GetPriceString();
    }

    @Test
    public void testStart() throws Exception {

    }

    @Test
    public void testGetSOAPString() throws Exception {

    }

    @Test
    public void testGetPriceString() throws Exception {

    }
}
