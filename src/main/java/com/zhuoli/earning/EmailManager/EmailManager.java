package com.zhuoli.earning.EmailManager;

import com.joanzapata.utils.Strings;
import com.zhuoli.earning.RunMe;
import org.junit.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.search.FlagTerm;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Created by zhuoli on 7/9/16.
 */
public class EmailManager {

    public static final Pattern INBOX_FOLDER_REGX_PATTERN = Pattern.compile("inbox", Pattern.CASE_INSENSITIVE);

    private final boolean disableEmail;

    private static EmailManager instance;
    private static String EmailRecipient;
    private String username;
    private String password;
    private Session sendSession = null;
    private boolean isAuthenticated = false;
    private Store receiveStore = null;


    /**
     * Constructor
     */
    private EmailManager() {
        if (RunMe.CMD.hasOption(RunMe.DISABLE_EMAIL)){
            this.disableEmail = true;
        }else{
            this.disableEmail = false;
        }
    }

    /**
     * Email manager constructor.
     *
     * @param username: Username
     * @param password: Password
     * @param recipient: Email recipient
     */
    private EmailManager(String username, String password, String recipient) {
        this.username = username;
        this.password = password;
        EmailManager.EmailRecipient = recipient;
        if (RunMe.CMD.hasOption(RunMe.DISABLE_EMAIL)){
            this.disableEmail = true;
        }else{
            this.disableEmail = false;
        }
    }

    /**
     * Initialize EmailMananger from XML configuration file.
     *
     * @param pathString : Configuration file path
     * @return: Emailmanager
     */

    public static EmailManager GetAndInitializeEmailmanager(String pathString) {

        // Data validation
        Assert.assertNotNull(pathString);

        if (EmailManager.instance != null)
            return EmailManager.instance;


        Path currentPath = Paths.get("./");
        System.out.println("Currrent path: " + currentPath.toAbsolutePath());
        Path path = Paths.get("src", "resources", pathString);

        if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS))
            path = Paths.get(pathString);

        if (Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
            try {
                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document doc = builder.parse(new DataInputStream(new FileInputStream(path.toFile())));
                Element documentElement = doc.getDocumentElement();
                Element emailsNode = (Element) documentElement.getElementsByTagName("Emails").item(0);

                String emailUser = emailsNode.getElementsByTagName("User").item(0).getTextContent();
                String emailPassoword = emailsNode.getElementsByTagName("Password").item(0).getTextContent();
                String emailRecipient = emailsNode.getElementsByTagName("Recipient").item(0).getTextContent();

                EmailManager.instance = new EmailManager(emailUser, emailPassoword, emailRecipient);
            } catch (Exception e) {
                Logger.getGlobal().log(Level.SEVERE, "Failed to read configuration file from " + pathString, e);
                EmailManager.instance = new EmailManager();
            }
        }



        return EmailManager.instance;
    }

    /**
     * EmailRecipient getter.
     *
     * @return: Email recipient
     */
    public static String getEmailRecipient() {
        return EmailRecipient;
    }


    /**
     * Authenticate Gmail credentail.
     *
     * @throws NoSuchProviderException
     */
    public void Authenticate() throws NoSuchProviderException {

        if (this.disableEmail)
            return;;

        // Verify credential
        Scanner scan = new Scanner(System.in);
        while (!this.isAuthenticated) {

            // Read credential from console if not set
            if (this.username == null || this.username.isEmpty() || this.password == null || this.password.isEmpty()) {

                // Loop until authentication succeed or exception raised
                System.out.println("Email:" + username + "\nPassword:");
                this.password = String.valueOf(scan.nextLine());
            }
            this.isAuthenticated = this.EmailAuthenticate(this.username, this.password);

            if (!this.isAuthenticated) {
                this.password = null;
            }
        }

        // Initialize Send Session
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.mail.yahoo.com");
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
    private static final String SSL_EMAIL_SOLUTION =
            "QQ email using RC4 for SSL connection, but RC4 algorithm is disabled …" +
            "\n" +
            "解决方法: http://www.xiaotanzhu.com/2016/07/30/use-rc4-in-tencent-mail.html\n" +
            "启用Java的RC4算法\n" +
            "没有去深研究为什么JDK会默认禁止这个算法，也不知道腾讯为什么会重新选择了一个JDK默认禁用的算法（7月29日之前是没有问题的）。但由于需要用到QQ邮箱，所以必须得开启这个算法。开启步骤如下：\n" +
            "Go to the Java JRE installation folder: {JRE_HOME}\\lib\\security\\\n" +
            "Locate java.security file.\n" +
            "Make a backup copy of the file.\n" +
            "Edit the java.security file with a text editor software (for example Notepad) according to the example further below.\n" +
            "534c534\n" +
            "- jdk.tls.disabledAlgorithms=SSLv3, RC4, MD5withRSA, DH keySize < 768\n" +
            "+ jdk.tls.disabledAlgorithms=SSLv3, MD5withRSA, DH keySize < 768\n" +
            "591c591\n" +
            "-         RC4_128, RC4_40, DES_CBC, DES40_CBC\n" +
            "+         DES_CBC, DES40_CBC\n" +
            "启用协议\n" +
            "在POP/SMTP的协议中增加最新的TLSv1.2协议：\n" +
            "prop.setProperties(\"mail.pop3s.ssl.protocols\", \"TSLv1 TSLv1.1 TLSv1.2\");\n" +
            "或\n" +
            "prop.setProperties(\"mail.smtps.ssl.protocols\", \"TSLv1 TSLv1.1 TLSv1.2\");";
    public boolean EmailAuthenticate(String username, String password) {
        if (this.disableEmail){
            return true;
        }

        Properties prop = new Properties();

        // Fix RC4 disabled problem in Java 8: http://www.xiaotanzhu.com/2016/07/30/use-rc4-in-tencent-mail.html
        prop.setProperty("mail.imaps.ssl.ciphersuites", "SSL_RSA_WITH_RC4_128_SHA");
        prop.setProperty("mail.imaps.ssl.protocols", "TLSv1 TLSv1.1 TLSv1.2");
        Session session = Session.getInstance(prop);
        try {

            this.receiveStore = session.getStore("imaps");
            this.receiveStore.connect("imap.exmail.qq.com", 993, username, password);
            return true;
        } catch (MessagingException e) {
            if (e.getMessage().contains("protocol is disabled or cipher suites are inappropriate")){
                System.err.println(SSL_EMAIL_SOLUTION);
                System.out.println("Press any key to exit.");
                try {
                    System.in.read();
                    System.exit(1);
                }catch (Exception exc){
                    // does nothing
                }
            }
            System.err.println("Password or user name incorrect, please try again.\nUsername: " + username + "\n" + "inner message '" + e.getMessage()+"'");
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
        if (this.disableEmail){
            return true;
        }
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

        if (this.disableEmail){
            String fakeContent = "Your Order Has Been Executed!\n" +
                    "\n" +
                    "Hi Zhuoli, \n" +
                    "\n" +
                    "Your market order to buy 10 shares of JKS was executed at an average price of $19.95 on July 6th 2017 at 3:27 PM. \n" +
                    "\n" +
                    "Your trade confirmation will be available in your account settings on Robinhood in one trading day. \n" +
                    "\n" +
                    "If you have any questions, please contact support@robinhood.com. \n" +
                    "\n" +
                    "Sincerely,\n" +
                    "The Robinhood Team \n" +
                    "robinhood.com";
            MonitorEmail monitorEmail = new MonitorEmail("inbox", "Fake robinhood order", "nicai@nicai.com", emailRecipient, fakeContent);
            emailList.add(monitorEmail);
            return emailList.toArray(new MonitorEmail[0]);
        }

        if (this.receiveStore == null)
            this.Authenticate();

        if (!this.receiveStore.isConnected())
            this.receiveStore.connect("imap.exmail.qq.com", 993, this.username, this.password);

        Folder[] allFolders = this.receiveStore.getDefaultFolder().list("*");
        Optional<Folder> optionalInboxFolder =
                Arrays.stream(allFolders)
                        .filter(folder -> EmailManager.INBOX_FOLDER_REGX_PATTERN.matcher(folder.getFullName()).matches())
                        .findAny();

        String[] names = Arrays.stream(allFolders).map(folder -> folder.getFullName()).toArray(String[]::new);
        Assert.assertTrue("Inbox doesn't exit in target mailbox: " + Arrays.toString(names), optionalInboxFolder.isPresent());

        Folder infolder = optionalInboxFolder.get();

        try {

            infolder.open(Folder.READ_WRITE);

            /*  Get the messages which is unread in the Inbox*/
            Message messages[] = infolder.search(new FlagTerm(new Flags(Flags.Flag.SEEN), isSeen));

            if (!infolder.isOpen())
                infolder.open(Folder.READ_ONLY);

            for (Message msg : messages) {
                try {
                    // Reopen folder if close
                    if (!infolder.isOpen())
                        infolder.open(Folder.READ_WRITE);
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
                    MonitorEmail email = new MonitorEmail("INBOX", msg.getSubject(), this.username, addresses[0].toString(), content);
                    emailList.add(email);
                } catch (Exception exc) {
                    Logger.getGlobal().log(Level.WARNING, "Failed to receive email from: " + emailRecipient, exc);
                }
            }
        } finally {
            if (infolder!=null && infolder.isOpen())
                infolder.close(false);
            if (this.receiveStore != null && this.receiveStore.isConnected())
                this.receiveStore.close();
        }
        return emailList.toArray(new MonitorEmail[0]);
    }
}
