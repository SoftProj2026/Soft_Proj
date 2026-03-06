package Service;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class SmtpEmailSender implements EmailSender {

    private final String smtpUsername;
    private final String smtpPassword;

    public SmtpEmailSender() {
        this.smtpUsername = System.getenv("EMAIL_USERNAME");
        this.smtpPassword = System.getenv("EMAIL_PASSWORD");
    }

    public SmtpEmailSender(String username, String password) {
        this.smtpUsername = username;
        this.smtpPassword = password;
    }

    public static String getCompanyEmail() {
        return "remaajomaa842@gmail.com";
    }

    public static String getEnvCompanyEmail() {
        return getCompanyEmail();
    }

    @Override
    public void send(String fromIgnored, String to, String subject, String body) {

        System.out.println("[SmtpEmailSender] smtpUsername=" + smtpUsername);
        System.out.println("[SmtpEmailSender] smtpPassword set? " + (smtpPassword != null && !smtpPassword.trim().isEmpty()));
        System.out.println("[SmtpEmailSender] to=" + to);

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

        // ✅ مهم: إجبار Java على TLS 1.2/1.3 (بعض البيئات تفشل بدونها)
        System.setProperty("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");
        System.setProperty("https.protocols", "TLSv1.2,TLSv1.3");

        Properties props = new Properties();

        // ✅ Debug: سيطبع تفاصيل SMTP في Eclipse Console
        props.put("mail.debug", "true");

        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        // STARTTLS
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");

        // ✅ timeouts
        props.put("mail.smtp.connectiontimeout", "15000");
        props.put("mail.smtp.timeout", "15000");
        props.put("mail.smtp.writetimeout", "15000");

        // ✅ بعض الشبكات/الـ proxies تحتاج هذا
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
            System.out.println("[SmtpEmailSender] Transport.send OK");

        } catch (MessagingException e) {
            System.out.println("[SmtpEmailSender] MessagingException: " + e.getMessage());
            throw new RuntimeException("SMTP failed: " + e.getMessage(), e);
        } catch (Exception e) {
            System.out.println("[SmtpEmailSender] Exception: " + e.getMessage());
            throw new RuntimeException("SMTP failed: " + e.getMessage(), e);
        }
    }
}