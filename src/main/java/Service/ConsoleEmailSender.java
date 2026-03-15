package Service;

/**
 * {@link EmailSender} implementation that prints email contents to the console instead of sending them.
 *
 * <p>This sender is useful for local development and testing when SMTP credentials are not configured.</p>
 * @author remaa
 * @version 1.0
 */
public class ConsoleEmailSender implements EmailSender {

    /**
     * Logs an email message to standard output.
     *
     * @param from    sender email address
     * @param to      recipient email address
     * @param subject email subject
     * @param body    email body
     */
    @Override
    public void send(String from, String to, String subject, String body) {
        System.out.println("[EMAIL] From: " + from);
        System.out.println("[EMAIL] To: " + to);
        System.out.println("[EMAIL] Subject: " + subject);
        System.out.println("[EMAIL] Body:\n" + body);
        System.out.println("--------------------------------------------------");
    }
}