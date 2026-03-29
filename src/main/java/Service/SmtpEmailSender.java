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
 * This implementation allows disabling the real network send per-instance (useful for tests).
 */
public class SmtpEmailSender implements EmailSender {

    private final String smtpUsername;
    private final String smtpPassword;

    /**
     * If true, Transport.send(msg) will be called. Default is determined from env/system property,
     * but can be overridden by using the constructor overload that accepts performRealSend.
     */
    private final boolean performRealSend;

    public SmtpEmailSender() {
        this(System.getenv("EMAIL_USERNAME"), System.getenv("EMAIL_PASSWORD"), true);
    }
    public SmtpEmailSender(String username, String password) {
        this(username, password, isRealSendEnabled());
    }

    /**
     * New constructor: explicitly control whether to perform real send for this instance.
     *
     * @param username smtp username
     * @param password smtp password
     * @param performRealSend whether to call Transport.send (true) or skip (false)
     */
    public SmtpEmailSender(String username, String password, boolean performRealSend) {
        this.smtpUsername = username;
        this.smtpPassword = password;
        this.performRealSend = performRealSend;
    }

    public static String getCompanyEmail() { return "remaajomaa842@gmail.com"; }
    public static String getEnvCompanyEmail() { return getCompanyEmail(); }

    private static boolean isRealSendEnabled() {
        String prop = System.getProperty("smtp.send.real");
        if (prop != null && prop.equalsIgnoreCase("true")) return true;
        String env = System.getenv("SMTP_SEND_REAL");
        return env != null && env.equalsIgnoreCase("true");
    }

    @Override
    public void send(String fromIgnored, String to, String subject, String body) {
        if (smtpUsername == null || smtpUsername.trim().isEmpty()
                || smtpPassword == null || smtpPassword.trim().isEmpty()) {
            throw new IllegalStateException(
                    "Missing SMTP credentials. Set EMAIL_USERNAME and EMAIL_PASSWORD environment variables or use explicit constructor."
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

            if (performRealSend) {
                Transport.send(msg);
            } else {
                System.out.println("[SmtpEmailSender] performRealSend=false -> skipping Transport.send()");
            }
        } catch (MessagingException e) {
            throw new RuntimeException("SMTP failed: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("SMTP failed: " + e.getMessage(), e);
        }
    }
}