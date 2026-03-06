package Service;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

public class EmailService {
    private final String username;
    private final String password;
    // ثابت: بريد الشركة  
    private final String companyEmail = "remaajomaa842@gmail.com";

    public EmailService(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * يرسل الإيميل إلى المستلم وإلى بريد الشركة معاً (TO لجميع العناوين).
     */
    public void sendEmail(String to, String subject, String body) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            // هنا أضف المستلمين معاً
            InternetAddress[] recipients = {
                new InternetAddress(to),
                new InternetAddress(companyEmail)
            };
            message.setRecipients(Message.RecipientType.TO, recipients);
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);

            System.out.println("Email sent successfully to " + to + " and " + companyEmail);
        } catch (MessagingException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to send email", e);
        }
    }
}