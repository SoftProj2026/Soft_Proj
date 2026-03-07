package Service;

/**
 * Email utility service that sends messages from a fixed company address to a fixed user address.
 *
 * <p>This class is typically used for demonstrations or constrained environments where recipient addressing
 * is not dynamic.</p>
 */
public class FixedEmailService {

    private static final String COMPANY_FROM = "remaajomaa842@gmail.com";
    private static final String FIXED_USER_TO = "remaajomaa70@gmail.com";

    private final EmailSender sender;

    /**
     * Creates the fixed email service.
     *
     * @param sender email sender implementation
     */
    public FixedEmailService(EmailSender sender) {
        this.sender = sender;
    }

    /**
     * Sends an email message to the fixed user recipient.
     *
     * @param subject email subject
     * @param body    email body
     */
    public void sendToFixedUser(String subject, String body) {
        sender.send(COMPANY_FROM, FIXED_USER_TO, subject, body);
    }
}