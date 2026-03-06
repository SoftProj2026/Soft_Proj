package Test;

import Service.EmailSender;
import Service.SmtpEmailSender;

public class testemail {
    public static void main(String[] args) {
        try {
            EmailSender sender = new SmtpEmailSender(
                    "remaajomaa842@gmail.com",
                    "PUT_REAL_GMAIL_APP_PASSWORD_HERE"
            );

            sender.send(
                    "remaajomaa842@gmail.com",
                    "remaajomaa842@gmail.com",
                    "Test email",
                    "Hello, this is a test."
            );

            System.out.println("Sent.");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("FAILED: " + e.getMessage());
        }
    }
}