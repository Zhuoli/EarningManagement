package ResultPublisher.EmailManager;

/**
 * Created by zhuoli on 7/10/16.
 */
public class MonitorEmail {

    public String Folder;

    public String Subject;

    public String Recipient;

    public String From;

    public String Content;

    public MonitorEmail(String folder, String subject, String recipient, String from, String content) {
        this.Folder = folder;
        this.Subject = subject;
        this.Recipient = recipient;
        this.From = from;
        this.Content = content;
    }
}
