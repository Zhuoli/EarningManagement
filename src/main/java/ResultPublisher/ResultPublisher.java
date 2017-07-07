package ResultPublisher;

import EmailManager.EmailManager;
import PriceMonitor.PriceMonitor;
import com.joanzapata.utils.Strings;

import javax.mail.NoSuchProviderException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import DataManager.StockRecord;

/**
 * User interactive via Email
 * Created by zhuoli on 6/23/16.
 */
public class ResultPublisher {

    EmailManager emailUser = null;


    private ResultPublisher() {
    }

    /**
     * Get publisher instance
     *
     * @return
     */
    public static ResultPublisher GetInstance() {

        return new ResultPublisher();
    }

    /**
     * Authenticate
     *
     * @return
     * @throws NoSuchProviderException
     */
    public Optional<ResultPublisher> CollectInformationAndAuthenticate(){
        if (this.emailUser == null) {
            this.emailUser = EmailManager.GetAndInitializeEmailmanager("resourceConfig.xml");
        }
        try {
            this.emailUser.Authenticate();
        }catch (NoSuchProviderException exc){
            return Optional.empty();
        }
        return Optional.of(this);
    }

    /**
     * Entry of result publisher thread.
     */
    public void Start() {
        try {
            int errorCount = 0;
            while (true) {
                try {
                    this.threadTask();
                    errorCount = 0;
                } catch (Exception e) {
                    if (++errorCount == 3)
                        throw e;
                }
            }
        } catch (InterruptedException exc) {
            Logger.getGlobal().severe("Price Prophet thread Interrupted: " + exc.getMessage());
        } catch (Exception exc) {
            Logger.getGlobal().log(Level.SEVERE, "ResultPublisher thread crashed.", exc);
        }
    }

    private void threadTask() throws Exception {

        // Get stock prices for each data item
        StockRecord[] stockItems = PriceMonitor.GetStocks();

        // Skip if items are null or empty
        if (stockItems == null || stockItems.length == 0) {
            System.out.println("Items are null or empty, sleep a while...");
            Thread.sleep(10 * 1000);
            return;
        }

        StringBuilder reportBuilder = new StringBuilder();

        reportBuilder.append(System.lineSeparator());
        reportBuilder.append(Strings.format("*************** Stock Report on {date} ***************").with("date", LocalDateTime.now().toString()).build());
        reportBuilder.append(System.lineSeparator());
        reportBuilder.append("Stock Price Table:");
        reportBuilder.append(System.lineSeparator());
        reportBuilder.append(this.GenerateLatestPriceReport(stockItems));
        reportBuilder.append(System.lineSeparator());
        reportBuilder.append("Earning Report Date:");
        reportBuilder.append(System.lineSeparator());
        reportBuilder.append(this.GenerateQuartelyReportDate(stockItems));
        reportBuilder.append(System.lineSeparator());
        reportBuilder.append("-------------------------Summary------------------------------");
        reportBuilder.append(System.lineSeparator());
        reportBuilder.append(this.GenerateEarningReport(stockItems));
        reportBuilder.append(System.lineSeparator());
        reportBuilder.append("**************************************************************");

        System.out.println(reportBuilder.toString());

        // Buying value
        Thread.sleep(10 * 1000);
    }

    private String GenerateQuartelyReportDate(StockRecord[] stockItems) {
        StringBuilder sb = new StringBuilder();
        for (StockRecord item : stockItems) {
            if (item.getReportDate() != null) {
                Date earningReportdate = item.getReportDate();
                sb.append(String.format("%1$-8s quarterly earning report date: %2$s ", item.getSymbol(), earningReportdate));
                sb.append(System.lineSeparator());
            }
        }
        return sb.toString();
    }

    private String GenerateLatestPriceReport(StockRecord[] stockItems) {
        StringBuilder sb = new StringBuilder();
        for (StockRecord item : stockItems) {
            // String Format: https://sharkysoft.com/archive/printf/docs/javadocs/lava/clib/stdio/doc-files/specification.htm
            String line = String.format("Symbol: %1$-8s Price: %2$6.2f Shares: %3$4d Earning: %4$8.2f %5$s",
                    item.getSymbol(), item.getCurrentPrice(), item.getShares(), (item.getCurrentPrice() - item.getSharedAverageCost()) * item.getShares(), System.lineSeparator());
            sb.append(line);
        }
        return sb.toString();
    }

    private String GenerateEarningReport(StockRecord[] stockItems) {
        double baseValue = Arrays.stream(stockItems).map(item -> item.getSharedAverageCost() * item.getShares()).reduce((a, b) -> a + b).get();
        double currentValue = Arrays.stream(stockItems).map(item -> item.getCurrentPrice() * item.getShares()).reduce((a, b) -> a + b).get();
        StringBuilder earningStringBuilder = new StringBuilder();
        earningStringBuilder.append(String.format("Buying price: %.2f" + System.lineSeparator(), baseValue));
        earningStringBuilder.append(String.format("Current value: %.2f" + System.lineSeparator(), currentValue));
        return earningStringBuilder.toString();
    }
}
