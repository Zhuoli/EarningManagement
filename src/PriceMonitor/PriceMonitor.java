package src.PriceMonitor;

import src.Utility.Log;

/**
 * Created by zhuoli on 6/23/16.
 */
public class PriceMonitor {

    public void Start() {
        try

        {
            while (true) {
                Thread.sleep(10 * 1000);
            }
        } catch (InterruptedException exc) {
            Log.PrintAndLog("Price monitor thread Interrupted: " + exc.getMessage());
        }
    }
}
