package Service;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

/**
 * SMTP email service implementation using Jakarta Mail.
 *
 * <p>This class sends plain-text emails through Gmail SMTP using credentials provided at construction time.
 * It supports sending to a target recipient and also includes the company email as an additional recipient.</p>
 *
 * <p>For local usage, the demo runner loads credentials from a {@code .env} file using {@link Dotenv}
 * and sends a test reminder email.</p>
 * @author remaa
 * @version 1.0
 */
public class EmailService {

    private final String username;
    private final String password;

    private final String companyEmail = "remaajomaa842@gmail.com";

    /**
     * Creates an SMTP email service.
     *
     * @param username SMTP username (email address)
     * @param password SMTP password (typically a Gmail App Password)
     */
    public EmailService(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Sends an email message.
     *
     * <p>The email is sent to both:</p>
     * <ul>
     *   <li>The provided {@code to} recipient</li>
     *   <li>The configured company email</li>
     * </ul>
     *
     * @param to      recipient email address
     * @param subject subject line
     * @param body    plain-text body
     * @throws RuntimeException if sending fails
     */
    public void sendEmail(String to, String subject, String body) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);

            message.setFrom(new InternetAddress(username));

            InternetAddress[] recipients = {
                    new InternetAddress(to),
                    new InternetAddress(companyEmail)
            };
            message.setRecipients(Message.RecipientType.TO, recipients);

            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    /**
     * Sends a demonstration reminder email using credentials loaded from {@code .env}.
     *
     * <p>Required environment variables (in {@code .env}):</p>
     * <ul>
     *   <li>{@code EMAIL_USERNAME}</li>
     *   <li>{@code EMAIL_PASSWORD}</li>
     * </ul>
     *
     * @throws IllegalStateException if credentials are missing from {@code .env}
     */
    static void run() {
        Dotenv dotenv = Dotenv.load();
        String username = dotenv.get("EMAIL_USERNAME");
        String password = dotenv.get("EMAIL_PASSWORD");

        if (username == null || username.trim().isEmpty()
                || password == null || password.trim().isEmpty()) {
            throw new IllegalStateException(
                    "Missing EMAIL_USERNAME / EMAIL_PASSWORD in .env\n" +
                            "Example:\n" +
                            "EMAIL_USERNAME=your@gmail.com\n" +
                            "EMAIL_PASSWORD=your_app_password"
            );
        }

        EmailService emailService = new EmailService(username.trim(), password.trim());

        String subject = "Book Due Reminder";
        String body = "Dear user, Your Appointment is comming soon. Best regards";

        emailService.sendEmail("remaajomaa70@gmail.com", subject, body);
        emailService.sendEmail("remaajomaa842@gmail.com", subject, body);
    }

    /**
     * Application entry point for running the email demo.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        run();
    }
}