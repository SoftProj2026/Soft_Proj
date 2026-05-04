package service;

import domain.Appointment;
import domain.AppointmentStatus;
import domain.TimeSlot;
import domain.User;
import persistence.DataRepository;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Service responsible for sending email reminders for upcoming bookings.
 *
 * <p>This service applies the reminder rule:</p>
 * <ul>
 *   <li>The appointment must be {@link AppointmentStatus#CONFIRMED}.</li>
 *   <li>The appointment start time must be in the future.</li>
 *   <li>The appointment start time must be within the next 24 hours.</li>
 *   <li>The user must have a non-empty email address.</li>
 * </ul>
 *
 * <p>Email delivery is performed via the injected {@link EmailSender} abstraction to support mocking in tests
 * and swapping the underlying email transport.</p>
 * @author qussaialaw
 * @version 1.0
 */
public class BookingEmailReminderService {

    private final DataRepository repo;
    private final EmailSender emailSender;

    /**
     * Creates the reminder service.
     *
     * @param repo        repository used to resolve user email and booking data
     * @param emailSender sender used to deliver emails
     */
    public BookingEmailReminderService(DataRepository repo, EmailSender emailSender) {
        this.repo = repo;
        this.emailSender = emailSender;
    }

    /**
     * Sends a reminder email if the appointment starts within 24 hours and is still in the future.
     *
     * @param a appointment to evaluate for reminder eligibility
     */
    public void send24hReminderIfNeeded(Appointment a) {
        if (a == null) return;
        if (a.getStatus() != AppointmentStatus.CONFIRMED) return;
        if (a.getUser() == null || a.getUser().getUsername() == null) return;

        User user = findUser(a.getUser().getUsername());
        if (user == null) return;

        String to = user.getEmail() != null ? user.getEmail().trim() : "";
        if (to.isEmpty()) {
            return;
        }

        TimeSlot slot = a.getSlot();
        if (slot == null || slot.getStartDateTime() == null) return;

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = slot.getStartDateTime();

        if (!start.isAfter(now)) return;

        Duration until = Duration.between(now, start);
        if (until.compareTo(Duration.ofHours(24)) > 0) return;

        long totalMinutes = until.toMinutes();
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;

        String from = getEnvCompanyEmail();
        if (from.isEmpty()) from = "noreply@example.com";

        String displayName = safeDisplayName(user);
        String cat = (slot.getCategory() != null) ? slot.getCategory().getName() : "N/A";

        String subject = "Reminder: your appointment is in " + hours + "h " + minutes + "m";

        String body =
                "Hello " + displayName + ",\n\n" +
                        "This is a reminder that your appointment starts soon.\n" +
                        "Time remaining: " + hours + "h " + minutes + "m\n\n" +
                        "Booking Details:\n" +
                        "- Category: " + cat + "\n" +
                        "- Start Time: " + start + "\n" +
                        "- Appointment ID: #" + a.getId() + "\n\n" +
                        "Thank you,\n" +
                        "QR Booking Team";

        emailSender.send(from, to, subject, body);
    }

    /**
     * Finds a user by username in the repository.
     *
     * @param username username to search for
     * @return matching user or {@code null} if not found
     */
    private User findUser(String username) {
        for (User u : repo.getUsers()) {
            if (u != null && u.getUsername() != null && u.getUsername().equalsIgnoreCase(username)) {
                return u;
            }
        }
        return null;
    }

    /**
     * Builds a safe display name for email greeting.
     *
     * @param u user
     * @return full name if available, otherwise username, otherwise empty string
     */
    private static String safeDisplayName(User u) {
        if (u == null) return "";
        String first = u.getFirstName() != null ? u.getFirstName().trim() : "";
        String last = u.getLastName() != null ? u.getLastName().trim() : "";
        String full = (first + " " + last).trim();
        return full.isEmpty() ? (u.getUsername() != null ? u.getUsername() : "") : full;
    }

    /**
     * Attempts to read the company email address from environment variables or a .env file.
     *
     * @return company email (may be empty if not configured)
     */
    private static String getEnvCompanyEmail() {
        String mail = System.getenv("EMAIL_USERNAME");
        if (mail != null && !mail.trim().isEmpty()) return mail.trim();

        try {
            io.github.cdimascio.dotenv.Dotenv dotenv = io.github.cdimascio.dotenv.Dotenv.load();
            mail = dotenv.get("EMAIL_USERNAME");
            return mail != null ? mail.trim() : "";
        } catch (Throwable ex) {
            return "";
        }
    }
}