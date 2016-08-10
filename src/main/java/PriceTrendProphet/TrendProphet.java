package PriceTrendProphet;

import java.util.logging.Logger;

/**
 * Created by zhuoli on 6/23/16.
 */
public class TrendProphet {

    static int count = 0;

    public void Start() {
        try

        {
            while (true) {
                Thread.sleep(3 * 1000);
                System.out.println("Hello TrendProphet is running: " + TrendProphet.count++);
            }
        } catch (InterruptedException exc) {
            Logger.getGlobal().severe("Price Prophet thread Interrupted: " + exc.getMessage());
        }
    }
}
