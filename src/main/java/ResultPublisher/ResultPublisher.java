package ResultPublisher;

import ResultPublisher.EmailManager.EmailManager;
import Utility.Log;

import javax.mail.NoSuchProviderException;

/**
 * User interactive via Email
 * Created by zhuoli on 6/23/16.
 */
public class ResultPublisher {

    static int count = 0;

    EmailManager emailUser = null;

    public static ResultPublisher GetInstance() {

        return new ResultPublisher();
    }

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
                Thread.sleep(3 * 1000);
                System.out.println("Hello ResultPublisher is running: " + ResultPublisher.count++);
            }
        } catch (InterruptedException exc) {
            Log.PrintAndLog("Price Prophet thread Interrupted: " + exc.getMessage());
        }
    }
}
