package src.Utility;

/**
 * Created by zhuoli on 6/23/16.
 */
public class Email {

    private static Email instance;

    private String email;

    private String password;

    private Email(String emailAddress, String password) {

        this.email = emailAddress;
        this.password = password;
    }

    public static Email GetInstance(String emailAddress, String password) throws Exception {

        // Throw exception if instance has been initialized and with different email address
        if (Email.instance != null && Email.instance.email != emailAddress) {
            throw new Exception("Email instance has been initialized with different email address");
        }

        if (Email.instance == null)
            Email.instance = new Email(emailAddress, password);

        return Email.instance;
    }

    public boolean Authenticate() {
        return false;
    }
}
