package service;

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
 * <p>For local usage, the demo runner loads credentials from a local environment file using {@link Dotenv}
 * and sends a test reminder email.</p>
 *
 * @author remaa
 * @version 1.0
 */
public class EmailService {

    private static final String EMAIL_USERNAME_KEY = "EMAIL_USERNAME";
    private static final String EMAIL_SECRET_KEY = "EMAIL_SECRET";

    private final String username;
    private final String emailSecret;

    private final String companyEmail = "remaajomaa842@gmail.com";

    /**
     * Creates an SMTP email service.
     *
     * @param username    SMTP username, usually an email address
     * @param emailSecret SMTP authentication secret, usually an app-specific credential
     */
    public EmailService(String username, String emailSecret) {
        this.username = username;
        this.emailSecret = emailSecret;
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
                return new PasswordAuthentication(username, emailSecret);
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
     * Sends a demonstration reminder email using credentials loaded from a local environment file.
     *
     * <p>Required environment variables:</p>
     * <ul>
     *   <li>{@code EMAIL_USERNAME}</li>
     *   <li>{@code EMAIL_SECRET}</li>
     * </ul>
     *
     * @throws IllegalStateException if credentials are missing
     */
    static void run() {
        Dotenv dotenv = Dotenv.load();
        String username = dotenv.get(EMAIL_USERNAME_KEY);
        String emailSecret = dotenv.get(EMAIL_SECRET_KEY);

        if (isBlank(username) || isBlank(emailSecret)) {
            throw new IllegalStateException(
                    "Missing email credentials in local environment file.\n" +
                            "Example:\n" +
                            EMAIL_USERNAME_KEY + "=your@gmail.com\n" +
                            EMAIL_SECRET_KEY + "=your_app_credential"
            );
        }

        EmailService emailService = new EmailService(username.trim(), emailSecret.trim());

        String subject = "Book Due Reminder";
        String body = "Dear user, your appointment is coming soon. Best regards.";

        emailService.sendEmail("remaajomaa70@gmail.com", subject, body);
        emailService.sendEmail("remaajomaa842@gmail.com", subject, body);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * Application entry point for running the email demo.
     *
     * @param args command-line arguments, not used
     */
    public static void main(String[] args) {
        run();
    }
}