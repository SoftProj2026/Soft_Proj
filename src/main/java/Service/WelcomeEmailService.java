package Service;

import domain.User;

public class WelcomeEmailService {

    private final EmailSender emailSender;
    private final String companyEmail;

    public WelcomeEmailService(EmailSender emailSender, String companyEmail) {
        this.emailSender = emailSender;
        this.companyEmail = companyEmail != null ? companyEmail.trim() : "";
    }

    public void sendWelcomeEmail(User user) {
        if (user == null) return;

        String to = user.getEmail() != null ? user.getEmail().trim() : "";
        if (to.isEmpty()) return;

        String from = companyEmail.isEmpty() ? "noreply@example.com" : companyEmail;

        String name = (safe(user.getFirstName()) + " " + safe(user.getLastName())).trim();
        if (name.isEmpty()) name = user.getUsername() != null ? user.getUsername() : "User";

        String subject = "Welcome, " + name + "!";
        String body =
                "Hello " + name + ",\n\n" +
                "Your account has been created successfully.\n" +
                "Username: " + user.getUsername() + "\n\n" +
                "Thanks,\n" +
                "QR Booking Team";

        emailSender.send(from, to, subject, body);
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }
}