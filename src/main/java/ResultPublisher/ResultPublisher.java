package ResultPublisher;

import ResultPublisher.EmailManager.EmailManager;

import javax.mail.NoSuchProviderException;
import java.time.LocalDate;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

/**
 * User interactive via Email
 * Created by zhuoli on 6/23/16.
 */
public class ResultPublisher {

    private static ConcurrentLinkedQueue<String> messageQueue = new ConcurrentLinkedQueue<>();


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

    public static void RegisterMessage(String message) {
        ResultPublisher.messageQueue.add(message);
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

    public void Start() {
        try
        {
            while (true) {
                if (!messageQueue.isEmpty()) {
                    StringBuilder sb = new StringBuilder("********************" + System.lineSeparator() + "Publishing Message Result:  " + LocalDate.now());
                    String result = messageQueue.stream().reduce(sb.toString(), (a, b) -> a + System.lineSeparator() + b);
                    messageQueue.clear();
                    System.out.println(result + System.lineSeparator() + "********************");
                }
                Thread.sleep(30 * 1000);
            }
        } catch (InterruptedException exc) {
            Logger.getGlobal().severe("Price Prophet thread Interrupted: " + exc.getMessage());
        }
    }
}
