package src.ResultPublisher.EmailManager;

import com.joanzapata.utils.Strings;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.search.FlagTerm;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

/**
 * Created by zhuoli on 7/9/16.
 */
public class EmailManager {

    Session sendSession = null;
    String username = "digitcurrencymonitor@gmail.com";
    String password = "";
    boolean isAuthenticated = false;
    Store receiveStore = null;

    public EmailManager() {
    }

    /**
     * Authenticate Gmail credentail.
     *
     * @throws NoSuchProviderException
     */
    public void Authenticate() throws NoSuchProviderException {

        // Verify credential
        Session session = Session.getInstance(new Properties());
        this.receiveStore = session.getStore("imaps");
        Scanner scan = new Scanner(System.in);
        while (!this.isAuthenticated) {

            // Loop until authentication succeed or exception raised
            try {
                System.out.println("Passord:");
                this.password = String.valueOf(scan.nextLine());
                this.receiveStore.connect("imap.googlemail.com", 993, this.username, this.password);
                this.isAuthenticated = true;
            } catch (MessagingException e) {
                System.out.println("Password incorrect, please try again; " + e.getMessage());
            } finally {
                try {
                    this.receiveStore.close();
                } catch (MessagingException e) {
                }
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


    public MonitorEmail[] Receive(String folderName, String from) throws MessagingException {
        List<MonitorEmail> emailList = new LinkedList<>();

        if (this.receiveStore == null)
            this.Authenticate();

        this.receiveStore.connect("imap.googlemail.com", 993, this.username, this.password);

        Folder infolder = this.receiveStore.getFolder(folderName);
        infolder.open(Folder.READ_ONLY);

         /*  Get the messages which is unread in the Inbox*/
        Message messages[] = infolder.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));

        for (Message msg : messages) {
            try {
                Address[] addresses = msg.getFrom();
                if (!addresses[0].toString().toLowerCase().contains(from)) {
                    System.out.println(addresses[0].toString());
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

                MonitorEmail email = new MonitorEmail(folderName, msg.getSubject(), this.username, addresses[0].toString(), content);
                emailList.add(email);
            } catch (Exception exc) {
                System.out.println("\tERROR:" + exc.getMessage());
            }
        }

        this.receiveStore.close();
        return emailList.toArray(new MonitorEmail[0]);
    }
}
