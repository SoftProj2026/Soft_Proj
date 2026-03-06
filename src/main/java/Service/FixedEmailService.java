package Service;

public class FixedEmailService {

    private static final String COMPANY_FROM = "remaajomaa842@gmail.com";
    private static final String FIXED_USER_TO = "remaajomaa70@gmail.com";

    private final EmailSender sender;

    public FixedEmailService(EmailSender sender) {
        this.sender = sender;
    }

    public void sendToFixedUser(String subject, String body) {
        sender.send(COMPANY_FROM, FIXED_USER_TO, subject, body);
    }
}