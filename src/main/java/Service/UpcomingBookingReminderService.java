package Service;

import domain.Appointment;
import domain.AppointmentStatus;
import domain.TimeSlot;
import domain.User;
import persistence.DataRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds reminder messages for upcoming bookings.
 *
 * <p>A reminder is generated for each {@link AppointmentStatus#CONFIRMED} appointment that starts in the future
 * and within the next 24 hours.</p>
 * @author remaa
 * @version 1.0
 */
public class UpcomingBookingReminderService {

    private final DataRepository repo;

    /**
     * Creates a new reminder service.
     *
     * @param repo repository used to read appointments
     */
    public UpcomingBookingReminderService(DataRepository repo) {
        this.repo = repo;
    }

    /**
     * Builds reminder messages for all upcoming confirmed appointments for the given user within 24 hours.
     *
     * @param username username to build reminders for
     * @return list of messages; empty list if none
     */
    public List<String> buildReminderMessages(String username) {
        List<String> messages = new ArrayList<>();
        if (username == null || username.trim().isEmpty()) return messages;

        LocalDateTime now = LocalDateTime.now();

        User userObj = null;
        for (User u : repo.getUsers()) {
            if (u == null || u.getUsername() == null) continue;
            if (u.getUsername().equalsIgnoreCase(username)) {
                userObj = u;
                break;
            }
        }

        String displayName;
        if (userObj != null) {
            String first = safe(userObj.getFirstName());
            String last = safe(userObj.getLastName());
            String full = (first + " " + last).trim();
            displayName = full.isEmpty() ? username : full;
        } else {
            displayName = username;
        }

        for (Appointment a : repo.getAppointments()) {
            if (a == null) continue;
            if (a.getStatus() != AppointmentStatus.CONFIRMED) continue;
            if (a.getUser() == null || a.getUser().getUsername() == null) continue;
            if (!a.getUser().getUsername().equalsIgnoreCase(username)) continue;

            TimeSlot slot = a.getSlot();
            if (slot == null || slot.getStartDateTime() == null) continue;

            LocalDateTime start = slot.getStartDateTime();
            if (!start.isAfter(now)) continue;

            Duration until = Duration.between(now, start);
            if (until.toHours() >= 24) continue;

            long hours = until.toHours();
            long minutes = until.minusHours(hours).toMinutes();

            String msg = "Reminder for " + displayName + ":\n"
                    + "Your booking starts in: " + hours + "h " + minutes + "m\n"
                    + "Category: " + slot.getCategory().getName() + "\n"
                    + "Time: " + start;

            messages.add(msg);
        }

        return messages;
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }
}