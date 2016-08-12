package ResultPublisher;

import PriceMonitor.stock.StockItem;
import ResultPublisher.EmailManager.EmailManager;
import ResultPublisher.EmailManager.MonitorEmail;
import com.joanzapata.utils.Strings;

import javax.mail.NoSuchProviderException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * User interactive via Email
 * Created by zhuoli on 6/23/16.
 */
public class ResultPublisher {

    EmailManager emailUser = null;

    Supplier<StockItem[]> getStocksFuc;

    private ResultPublisher(Supplier<StockItem[]> getStocksFuc) {
        this.getStocksFuc = getStocksFuc;
    }

    /**
     * Get publisher instance
     *
     * @return
     */
    public static ResultPublisher GetInstance(Supplier<StockItem[]> getStocksFuc) {

        return new ResultPublisher(getStocksFuc);
    }

    /**
     * Authenticate
     * @return
     * @throws NoSuchProviderException
     */
    public ResultPublisher CollectInformationAndAuthenticate() throws NoSuchProviderException {
        if (this.emailUser == null) {
            this.emailUser = new EmailManager();
        }
        this.emailUser.Authenticate();
        return this;
    }

    // Result publisher
    public void Start() {
        try
        {
            while (true) {

                // Get stock prices for each data item
                StockItem[] stockItems = this.getStocksFuc.get();

                // Skip if items are null or empty
                if (stockItems == null || stockItems.length == 0) {
                    System.out.println("Items are null or empty, sleep a while...");
                    Thread.sleep(10 * 1000);
                    continue;
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

                MonitorEmail[] emails = this.emailUser.ReceiveUnreadEmails();

                if (emails != null && emails.length > 0) {
                    this.emailUser.Send(EmailManager.FROM, "Stock Report", reportBuilder.toString());
                }

                // Buying value
                Thread.sleep(5 * 1000);
            }
        } catch (InterruptedException exc) {
            Logger.getGlobal().severe("Price Prophet thread Interrupted: " + exc.getMessage());
        } catch (Exception exc) {
            Logger.getGlobal().log(Level.SEVERE, "ResultPublisher thread crashed.", exc);
        }
    }

    private String GenerateQuartelyReportDate(StockItem[] stockItems) {
        StringBuilder sb = new StringBuilder();
        for (StockItem item : stockItems) {
            if (item.getEarningReportDate().isPresent()) {
                LocalDate earningReportdate = item.getEarningReportDate().get();
                sb.append(String.format("%1$-8s quarterly earning report date: %2$s ", item.Symbol, earningReportdate));
                sb.append(System.lineSeparator());
            }
        }
        return sb.toString();
    }

    private String GenerateLatestPriceReport(StockItem[] stockItems) {
        StringBuilder sb = new StringBuilder();
        for (StockItem item : stockItems) {
            // String Format: https://sharkysoft.com/archive/printf/docs/javadocs/lava/clib/stdio/doc-files/specification.htm
            String line = String.format("Symbol: %1$-8s Price: %2$6.2f Shares: %3$4d Earning: %4$8.2f %5$s",
                    item.Symbol, item.Price, item.Shares, (item.Price - item.AverageCost) * item.Shares, System.lineSeparator());
            sb.append(line);
        }
        return sb.toString();
    }

    private String GenerateEarningReport(StockItem[] stockItems) {
        double baseValue = Arrays.stream(stockItems).map(item -> item.AverageCost * item.Shares).reduce((a, b) -> a + b).get();
        double currentValue = Arrays.stream(stockItems).map(item -> item.Price * item.Shares).reduce((a, b) -> a + b).get();
        StringBuilder earningStringBuilder = new StringBuilder();
        earningStringBuilder.append(String.format("Buying price: %.2f" + System.lineSeparator(), baseValue));
        earningStringBuilder.append(String.format("Current value: %.2f" + System.lineSeparator(), currentValue));
        return earningStringBuilder.toString();
    }
}
