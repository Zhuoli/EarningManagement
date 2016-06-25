package src.PriceMonitor;

import src.Utility.Log;

/**
 * Created by zhuoli on 6/23/16.
 */
public class PriceMonitor {

    static int count = 0;

    public void Start() {
        try

        {
            while (true) {
                Thread.sleep(3 * 1000);

                System.out.println("Hello PriceMonitor is running: " + PriceMonitor.count++);
            }
        } catch (InterruptedException exc) {
            Log.PrintAndLog("Price monitor thread Interrupted: " + exc.getMessage());
        }
    }
}
