package src.PriceMonitor;

import org.json.JSONArray;
import org.json.JSONObject;
import src.Utility.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by zhuoli on 6/23/16.
 */
public class PriceMonitor {

    /*
    {
        "symbol":"btc",
        "position":"1",
        "name":"Bitcoin",
        "market_cap":
            {"usd":10027475766.5,"eur":9022221320.908377,"cny":66609984565.49058,"gbp":7478220684.810005,"cad":12991998702.108059,"rub":640046658667.9008,"hkd":77800948345.17758,"jpy":1029790676044.674,"aud":13465145146.150362,"btc":"15715150.0"},
        "price":
            {"usd":638.077,"eur":574.10978075,"cny":4238.584077519,"gbp":475.86059852100004,"cad":826.71808428,"rub":40728.001875329996,"hkd":4950.697151829,"jpy":65528.529861300005,"aud":856.8257475249999,"btc":"1.0"},
        "supply":"15715150",
        "volume":
            {"usd":119284000,"eur":107325779.00000001,"cny":792373433.148,"gbp":88958786.532,"cad":154549121.76,"rub":7613813028.36,"hkd":925497955.6680001,"jpy":12250097019.6,"aud":160177537.29999998,"btc":"186909.0"},
        "change":"1.06","timestamp":"1467265624.828"
        }
     */
    static final String BASE_URL = "http://coinmarketcap-nexuist.rhcloud.com/api";

    static final String BTC = "btc";

    static int count = 0;
    InterruptedException exc

        try

        {
            while (true) {
                Thread.sleep(3 * 1000);

                System.out.println("Hello PriceMonitor is running: " + PriceMonitor.count++);
            }
        }

    catch(

    {
        Log.PrintAndLog("Price monitor thread Interrupted: " + exc.getMessage());
    }

    )

    public void Start() {

        URL soapUrl = null;

        try {
            soapUrl = new URL(PriceMonitor.BASE_URL + "/" + BTC);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return;
        }

        try {
            InputStream is = soapUrl.openStream();
            String result = "";
            JSONObject obj = new JSONObject(result);
            obj.get("price");
        } catch (Exception exc) {
        }
        try {
            try (InputStream is = soapUrl.openStream();
                 JSONReader rdr = Json.createReader(is)) {
                JSONObject obj = rdr.readObject();
                JSONArray results = obj.getJSONArray("data");
                for (JSONObject result : results.getValuesAs(JSONObject.class)) {
                    System.out.print(result.getJsonObject("from").getString("name"));
                    System.out.print(": ");
                    System.out.println(result.getString("message", ""));
                    System.out.println("-----------");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    }
}
