package src.ResultPublisher.EmailManager;

import com.joanzapata.utils.Strings;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.Scanner;

/**
 * Created by zhuoli on 7/9/16.
 */
public class EmailManager {

    Session session = null;
    String username = "";
    String password = "";
    boolean isAuthenticated = false;
    Store receiveStore = null;

    public EmailManager() {
    }

    public void Authenticate() throws NoSuchProviderException {

        Session session = Session.getDefaultInstance(new Properties());
        this.receiveStore = session.getStore("imaps");
        Scanner scan = new Scanner(System.in);
        this.username = "digitcurrencymonitor@gmail.com";
        while (!this.isAuthenticated) {

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
    }

    public boolean Send() {

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");

        this.session = Session.getDefaultInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("from@no-spam.com"));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse("to@no-spam.com"));
            message.setSubject("Testing Subject");
            message.setText("Dear Mail Crawler," +
                    "\n\n No spam to my email, please!");

            Transport.send(message);

            System.out.println("Done");

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    public void Receive(String folderName) throws MessagingException {
        if (this.receiveStore == null)
            this.Authenticate();

        this.receiveStore.connect("imap.googlemail.com", 993, this.username, this.password);

        Folder infolder = this.receiveStore.getFolder(folderName);
        infolder.open(Folder.READ_ONLY);

         /*  Get the messages which is unread in the Inbox*/
        Message messages[] = infolder.getMessages(); //infolder.search(new FlagTerm(new Flags(Flag.SEEN), false));

        for (Message msg : messages) {
            try {
                System.out.println(
                        Strings.format("Email in folder '{folderName}';\tSubject: '{subject}'; Text:{Text}").with("folderName", folderName).with("subject", msg.getSubject()).with("Text", msg.getContent().toString()).build());
            } catch (Exception exc) {
                System.out.println(exc.getMessage());
            }
        }

        this.receiveStore.close();
    }
}
