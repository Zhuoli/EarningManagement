package ResultPublisher.EmailManager;

import com.joanzapata.utils.Strings;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.search.FlagTerm;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by zhuoli on 7/9/16.
 */
public class EmailManager {

    private final static String FOLDER = "Inbox";
    private static String EmailRecipient;
    private String username;
    private String password;
    private Session sendSession = null;
    private boolean isAuthenticated = false;
    private Store receiveStore = null;
    /**
     * Constructor
     */
    public EmailManager() {
    }

    /**
     * Email manager constructor.
     *
     * @param username: Username
     * @param password: Password
     */
    public EmailManager(String username, String password, String recipient) {
        this.username = username;
        this.password = password;
        EmailManager.EmailRecipient = recipient;
    }

    /**
     * EmailRecipient getter.
     *
     * @return: Email recipient
     */
    public static String getEmailRecipient() {
        return EmailRecipient;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    /**
     * Authenticate Gmail credentail.
     *
     * @throws NoSuchProviderException
     */
    public void Authenticate() throws NoSuchProviderException {

        // Verify credential
        Scanner scan = new Scanner(System.in);
        while (!this.isAuthenticated) {

            // Read credential from console if not set
            if (this.username == null || this.username.isEmpty() || this.password == null || this.password.isEmpty()) {

                // Loop until authentication succeed or exception raised
                System.out.println("Email:" + username);
                System.out.print("Password:");
                this.password = String.valueOf(scan.nextLine());
            }
            this.isAuthenticated = this.EmailAuthenticate(this.username, this.password);

            if (!this.isAuthenticated) {
                this.password = null;
            }
        }

        // Initialize Send Session
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");

        // Get session instance
        this.sendSession = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });
    }

    /**
     * Email authenticate.
     *
     * @param username
     * @param password
     * @return
     */
    public boolean EmailAuthenticate(String username, String password) {
        Session session = Session.getInstance(new Properties());
        try {

            this.receiveStore = session.getStore("imaps");
            this.receiveStore.connect("imap.googlemail.com", 993, username, password);
            return true;
        } catch (MessagingException e) {
            System.out.println("Password incorrect, please try again; " + e.getMessage());
        } finally {
            if (this.receiveStore != null)
                try {
                    this.receiveStore.close();
                } catch (MessagingException e) {
                }
        }
        return false;
    }

    /**
     * Send email.
     *
     * @return
     * @throws NoSuchProviderException
     */
    public boolean Send(String recipient, String subject, String content) throws NoSuchProviderException {

        if (!this.isAuthenticated)
            this.Authenticate();

        try {

            Message message = new MimeMessage(sendSession);
            message.setFrom(new InternetAddress(this.username));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(recipient));
            message.setSubject(subject);
            message.setText(content);

            // Send
            Transport.send(message);

            // Interaction
            System.out.println(Strings.format("Email sent out to: {recipient}").with("recipient", recipient).build());

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    /**
     * ReceiveEmailsFrom emails for the given folder with given From.
     * @param  emailRecipient from address.
     * @param isSeen if only fetch seen email.
     * @return emails
     * @throws MessagingException
     */
    public MonitorEmail[] ReceiveEmailsFrom(String emailRecipient, boolean isSeen) throws MessagingException {
        List<MonitorEmail> emailList = new LinkedList<>();

        if (this.receiveStore == null)
            this.Authenticate();

        this.receiveStore.connect("imap.googlemail.com", 993, this.username, this.password);

        Folder infolder = this.receiveStore.getFolder(EmailManager.FOLDER);

        try {

            infolder.open(Folder.READ_WRITE);

            /*  Get the messages which is unread in the Inbox*/
            Message messages[] = infolder.search(new FlagTerm(new Flags(Flags.Flag.SEEN), isSeen));

            for (Message msg : messages) {
                try {
                    Address[] addresses = msg.getFrom();

                    // Skip the unrelated emails
                    if (!addresses[0].toString().toLowerCase().contains(emailRecipient)) {
                        continue;
                    }
                    String content = "";
                    Multipart multipart = (Multipart) msg.getContent();
                    for (int i = 0; i < multipart.getCount(); i++) {
                        BodyPart part = multipart.getBodyPart(i);
                        if (part.getContentType().toLowerCase().contains("text/plain") || part.getContentType().toLowerCase().contains("text/html")) {
                            content += part.getContent() + "\n\n";
                        }
                    }

                    // You might think the way to do this is to get the message, set the Flags.Flag.SEEN flag to true, and then call message.saveChanges(). Oddly, this is not the case.
                    // Instead, the JavaMail API Design Specification, Chapter 4, section "The Flags Class" states that the SEEN flag is implicitly set when the contents of a message are retrieved.
                    // http://stackoverflow.com/questions/7678919/javamail-mark-gmail-message-as-read
                    msg.getContent();
                    MonitorEmail email = new MonitorEmail(EmailManager.FOLDER, msg.getSubject(), this.username, addresses[0].toString(), content);
                    emailList.add(email);
                } catch (Exception exc) {
                    Logger.getGlobal().log(Level.WARNING, "Failed to receive email.", exc);
                }
            }
        } finally {
            infolder.close(false);
            this.receiveStore.close();
        }
        return emailList.toArray(new MonitorEmail[0]);
    }
}
