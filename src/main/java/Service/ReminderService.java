package Service;

import domain.Appointment;
import domain.AppointmentStatus;
import persistence.DataRepository;

import javax.swing.JOptionPane;
import javax.swing.Timer;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ReminderService {

    private final DataRepository repo;
    private final AuthService auth;

    private final int minutesBefore; // e.g. 60
    private Timer timer;

    private final Set<Integer> notifiedIds = new HashSet<>();
    private volatile List<Appointment> lastSoon = new ArrayList<>();

    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public ReminderService(DataRepository repo, AuthService auth, int minutesBefore) {
        this.repo = repo;
        this.auth = auth;
        this.minutesBefore = minutesBefore;
    }

    public void start() {
        if (timer != null) return;

        timer = new Timer(60_000, e -> checkAndNotify()); // every 1 min
        timer.setInitialDelay(0);
        timer.start();
    }

    public void stop() {
        if (timer != null) {
            timer.stop();
            timer = null;
        }
        lastSoon = new ArrayList<>();
        notifiedIds.clear();
    }

    public List<Appointment> getSoonAppointmentsSnapshot() {
        return new ArrayList<>(lastSoon);
    }

    private void checkAndNotify() {
        if (auth == null || !auth.isLoggedIn() || auth.getCurrentUser() == null) {
            lastSoon = new ArrayList<>();
            return;
        }

        String username = auth.getCurrentUser().getUsername();
        LocalDateTime now = LocalDateTime.now();

        List<Appointment> soon = repo.getAppointments().stream()
                .filter(a -> a.getStatus() == AppointmentStatus.CONFIRMED)
                .filter(a -> a.getUser() != null && a.getUser().getUsername().equalsIgnoreCase(username))
                .filter(a -> a.getSlot() != null && a.getSlot().getStartDateTime() != null)
                .filter(a -> {
                    long mins = Duration.between(now, a.getSlot().getStartDateTime()).toMinutes();
                    return mins >= 0 && mins <= minutesBefore;
                })
                .sorted(Comparator.comparing(a -> a.getSlot().getStartDateTime()))
                .collect(Collectors.toList());

        lastSoon = soon;

        List<Appointment> newOnes = soon.stream()
                .filter(a -> !notifiedIds.contains(a.getId()))
                .collect(Collectors.toList());

        if (newOnes.isEmpty()) return;

        for (Appointment a : newOnes) notifiedIds.add(a.getId());

        StringBuilder sb = new StringBuilder();
        sb.append("Upcoming appointments within ").append(minutesBefore).append(" minutes:\n\n");

        for (Appointment a : newOnes) {
            String cat = (a.getSlot().getCategory() != null) ? a.getSlot().getCategory().getName() : "N/A";
            LocalDateTime start = a.getSlot().getStartDateTime();
            long minsLeft = Duration.between(now, start).toMinutes();

            sb.append("- [#").append(a.getId()).append("] ")
              .append(cat)
              .append(" @ ").append(start.format(fmt))
              .append(" (in ").append(minsLeft).append(" min)")
              .append("\n");
        }

        JOptionPane.showMessageDialog(
                null,
                sb.toString(),
                "Reminder",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}