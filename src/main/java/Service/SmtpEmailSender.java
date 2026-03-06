package Service;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

public class SmtpEmailSender implements EmailSender {

    private final String smtpUsername;
    private final String smtpPassword;

    /** استخدم هذا الـ constructor للإنتاج, يعتمد على متغيرات النظام */
    public SmtpEmailSender() {
        this.smtpUsername = System.getenv("EMAIL_USERNAME");
        this.smtpPassword = System.getenv("EMAIL_PASSWORD");
    }
    /** استخدم هذا بالاختبار */
    public SmtpEmailSender(String username, String password) {
        this.smtpUsername = username;
        this.smtpPassword = password;
    }

    public static String getEnvCompanyEmail() {
        return System.getenv("EMAIL_USERNAME") != null ? System.getenv("EMAIL_USERNAME") : "remaajomaa842@gmail.com";
    }

    @Override
    public void send(String fromIgnored, String to, String subject, String body) {
        if (smtpUsername == null || smtpUsername.isEmpty() || smtpPassword == null || smtpPassword.isEmpty()) {
            throw new IllegalStateException("SMTP credentials are missing (EMAIL_USERNAME or EMAIL_PASSWORD)");
        }
        if (to == null || to.trim().isEmpty()) {
            throw new IllegalArgumentException("Recipient email is missing.");
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.starttls.enable", "true");

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

        } catch (Exception e) {
            throw new RuntimeException("SMTP send failed: " + e.getMessage(), e);
        }
    }
}