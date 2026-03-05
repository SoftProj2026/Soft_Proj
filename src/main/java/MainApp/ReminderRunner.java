package MainApp;

import Service.UpcomingBookingReminderService;
import domain.User;
import persistence.DataRepository;
import persistence.RepoStorage;

import javax.swing.*;
import java.time.Duration;
import java.util.List;

/**
 * Background reminder runner that periodically checks for upcoming bookings and shows popup reminders.
 *
 * <p>The runner checks every {@value #CHECK_EVERY_MINUTES} minutes.</p>
 */
public class ReminderRunner {

    private static final long CHECK_EVERY_MINUTES = 15;

    /**
     * Starts the reminder loop.
     *
     * @param args command-line arguments (not used)
     * @throws Exception if the thread sleep is interrupted
     */
    public static void main(String[] args) throws Exception {

        while (true) {

            DataRepository repo = RepoStorage.loadOrNew();
            UpcomingBookingReminderService svc = new UpcomingBookingReminderService(repo);

            for (User u : repo.getUsers()) {
                if (u == null || u.getUsername() == null) continue;

                List<String> msgs = svc.buildReminderMessages(u.getUsername());

                for (String msg : msgs) {
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                            null,
                            msg,
                            "Upcoming Booking Reminder",
                            JOptionPane.INFORMATION_MESSAGE
                    ));
                }
            }

            Thread.sleep(Duration.ofMinutes(CHECK_EVERY_MINUTES).toMillis());
        }
    }
}