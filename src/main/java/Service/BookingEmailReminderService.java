package Service;

import domain.Appointment;
import domain.AppointmentStatus;
import domain.TimeSlot;
import domain.User;
import persistence.DataRepository;

import java.time.Duration;
import java.time.LocalDateTime;

public class BookingEmailReminderService {

    private final DataRepository repo;
    private final EmailSender emailSender;

    private final String companyEmail;

    public BookingEmailReminderService(DataRepository repo, EmailSender emailSender, String companyEmail) {
        this.repo = repo;
        this.emailSender = emailSender;
        this.companyEmail = companyEmail != null ? companyEmail.trim() : "";
    }

    // NEW: send reminder for ONE appointment if it's within 24h
    public void send24hReminderIfNeeded(Appointment a) {
        if (a == null) return;
        if (a.getStatus() != AppointmentStatus.CONFIRMED) return;

        if (a.getUser() == null || a.getUser().getUsername() == null) return;

        User user = findUser(a.getUser().getUsername());
        if (user == null) return;

        String to = user.getEmail() != null ? user.getEmail().trim() : "";
        if (to.isEmpty()) {
            System.out.println("[BookingEmailReminderService] No user email found. Skipping.");
            return;
        }

        TimeSlot slot = a.getSlot();
        if (slot == null || slot.getStartDateTime() == null) return;

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = slot.getStartDateTime();

        if (!start.isAfter(now)) return;

        Duration until = Duration.between(now, start);

        // Only when it is < 24 hours (and positive)
        if (until.compareTo(Duration.ofHours(24)) >= 0) return;

        String from = companyEmail.isEmpty() ? "noreply@example.com" : companyEmail;

        String displayName = safeDisplayName(user);
        String cat = (slot.getCategory() != null) ? slot.getCategory().getName() : "N/A";

        String subject = "Reminder for " + displayName + ": booking within 24 hours";
        String body =
                "Hello " + displayName + ",\n\n" +
                "This is a reminder that you have an upcoming booking within 24 hours.\n\n" +
                "Category: " + cat + "\n" +
                "Start time: " + start + "\n" +
                "Appointment ID: #" + a.getId() + "\n\n" +
                "Thank you.";

        emailSender.send(from, to, subject, body);
    }

    private User findUser(String username) {
        for (User u : repo.getUsers()) {
            if (u == null || u.getUsername() == null) continue;
            if (u.getUsername().equalsIgnoreCase(username)) return u;
        }
        return null;
    }

    private static String safeDisplayName(User u) {
        if (u == null) return "";
        String first = u.getFirstName() != null ? u.getFirstName().trim() : "";
        String last = u.getLastName() != null ? u.getLastName().trim() : "";
        String full = (first + " " + last).trim();
        return full.isEmpty() ? (u.getUsername() != null ? u.getUsername() : "") : full;
    }
}