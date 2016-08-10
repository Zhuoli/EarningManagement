package ResultPublisher.EmailManager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by zhuoli on 8/10/16.
 */
public class EmailManagerTest {

    EmailManager emailManager;

    @Before
    public void SetUp() {
        this.emailManager = new EmailManager("digitcurrencymonitor@gmail.com", "");
    }

    @Test
    public void testReceive() throws Exception {
        MonitorEmail[] emails = this.emailManager.Receive("Inbox", "robotonyszu@gmail.com");
        Assert.assertTrue(emails != null && emails.length > 0);
    }

    @Test
    public void testAuthenticate() throws Exception {
        Assert.assertTrue(this.emailManager.EmailAuthenticate(this.emailManager.username, this.emailManager.password));
    }
}