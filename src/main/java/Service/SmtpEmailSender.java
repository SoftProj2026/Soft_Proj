package Service;

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
 * SMTP implementation of {@link EmailSender} using Jakarta Mail.
 *
 * <p>This sender is intended for production usage. Credentials are taken from:</p>
 * <ul>
 *   <li>Environment variables: {@code EMAIL_USERNAME} and {@code EMAIL_PASSWORD} (default constructor)</li>
 *   <li>Explicit constructor arguments (overload)</li>
 * </ul>
 *
 * <p>Gmail SMTP configuration:</p>
 * <ul>
 *   <li>Host: smtp.gmail.com</li>
 *   <li>Port: 587</li>
 *   <li>STARTTLS enabled</li>
 * </ul>
 * @author Qussai
 * @version 1.0
 */
public class SmtpEmailSender implements EmailSender {

    private final String smtpUsername;
    private final String smtpPassword;

    /**
     * Creates an SMTP sender using credentials from environment variables.
     */
    public SmtpEmailSender() {
        this.smtpUsername = System.getenv("EMAIL_USERNAME");
        this.smtpPassword = System.getenv("EMAIL_PASSWORD");
    }

    /**
     * Creates an SMTP sender with explicit credentials.
     *
     * @param username smtp username
     * @param password smtp password
     */
    public SmtpEmailSender(String username, String password) {
        this.smtpUsername = username;
        this.smtpPassword = password;
    }

    /**
     * Returns the application company email address.
     *
     * @return company email address
     */
    public static String getCompanyEmail() {
        return "remaajomaa842@gmail.com";
    }

    /**
     * Returns the company email as used by other services.
     *
     * @return company email address
     */
    public static String getEnvCompanyEmail() {
        return getCompanyEmail();
    }

    /**
     * Sends an email using Gmail SMTP.
     *
     * <p>The {@code fromIgnored} parameter is ignored because Gmail will always send from the authenticated account.</p>
     *
     * @param fromIgnored unused logical sender (ignored)
     * @param to         recipient email address
     * @param subject    email subject
     * @param body       email body
     */
    @Override
    public void send(String fromIgnored, String to, String subject, String body) {
        if (smtpUsername == null || smtpUsername.trim().isEmpty()
                || smtpPassword == null || smtpPassword.trim().isEmpty()) {
            throw new IllegalStateException(
                    "Missing SMTP env vars.\n" +
                            "Set in Windows/Eclipse:\n" +
                            "EMAIL_USERNAME=remaajomaa842@gmail.com\n" +
                            "EMAIL_PASSWORD=<Gmail App Password>"
            );
        }

        if (to == null || to.trim().isEmpty()) {
            throw new IllegalArgumentException("Recipient email is missing.");
        }

        System.setProperty("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");
        System.setProperty("https.protocols", "TLSv1.2,TLSv1.3");

        Properties props = new Properties();
        props.put("mail.debug", "true");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.connectiontimeout", "15000");
        props.put("mail.smtp.timeout", "15000");
        props.put("mail.smtp.writetimeout", "15000");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(smtpUsername, smtpPassword);
            }
        });

        try {
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(smtpUsername));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to.trim()));
            msg.setSubject(subject != null ? subject : "");
            msg.setText(body != null ? body : "");

            Transport.send(msg);
        } catch (MessagingException e) {
            throw new RuntimeException("SMTP failed: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("SMTP failed: " + e.getMessage(), e);
        }
    }
}