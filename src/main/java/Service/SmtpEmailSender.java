package Service;

import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class SmtpEmailSender implements EmailSender {

    private final String smtpUsername;
    private final String smtpPassword;

    public SmtpEmailSender(String smtpUsername, String smtpPassword) {
        this.smtpUsername = smtpUsername != null ? smtpUsername.trim() : "";
        this.smtpPassword = smtpPassword != null ? smtpPassword.trim() : "";
    }

    @Override
    public void send(String fromIgnored, String to, String subject, String body) {
        if (smtpUsername.isEmpty() || smtpPassword.isEmpty()) {
            throw new IllegalStateException("SMTP credentials are missing.");
        }
        if (to == null || to.trim().isEmpty()) {
            throw new IllegalArgumentException("Recipient email is missing.");
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new jakarta.mail.Authenticator() {
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