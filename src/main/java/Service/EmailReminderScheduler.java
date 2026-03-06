package Service;

import domain.Appointment;
import domain.AppointmentStatus;
import persistence.DataRepository;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Periodically checks upcoming CONFIRMED appointments and triggers 24-hour reminder emails.
 * Uses in-memory tracking to avoid re-sending the same appointment repeatedly during the same run.
 */
public class EmailReminderScheduler {

    private final DataRepository repo;
    private final AuthService auth;
    private final BookingEmailReminderService reminderService;
    private final ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
    private final Set<Integer> emailedAppointmentIds = new HashSet<>();
    private final long checkEveryMinutes;

    // Injectable clock (useful for unit testing)
    private final Clock clock;

    // اختياري: إذا ما عندك رغبة في Clock فيمكنك حذف هذا الكونستركتر والإبقاء فقط على واحد
    public EmailReminderScheduler(
        DataRepository repo,
        AuthService auth,
        BookingEmailReminderService reminderService,
        long checkEveryMinutes
    ) {
        this(repo, auth, reminderService, checkEveryMinutes, Clock.systemDefaultZone());
    }

    public EmailReminderScheduler(
        DataRepository repo,
        AuthService auth,
        BookingEmailReminderService reminderService,
        long checkEveryMinutes,
        Clock clock
    ) {
        this.repo = repo;
        this.auth = auth;
        this.reminderService = reminderService;
        this.checkEveryMinutes = checkEveryMinutes;
        this.clock = clock;
    }

    public void start() {
        exec.scheduleAtFixedRate(this::safeCheck, 0, checkEveryMinutes, TimeUnit.MINUTES);
    }

    public void stop() {
        exec.shutdownNow();
        emailedAppointmentIds.clear();
    }

    private void safeCheck() {
        try {
            checkAndSend();
        } catch (Exception ex) {
            System.out.println("[EmailReminderScheduler] ERROR: " + ex.getMessage());
        }
    }

    private void checkAndSend() {
        if (auth == null || !auth.isLoggedIn() || auth.getCurrentUser() == null) return;

        String username = auth.getCurrentUser().getUsername();
        LocalDateTime now = LocalDateTime.now(clock); // إذا أردت تزوير الوقت باختبار

        for (Appointment a : repo.getAppointments()) {
            if (a == null) continue;
            if (a.getStatus() != AppointmentStatus.CONFIRMED) continue;
            if (a.getUser() == null || a.getUser().getUsername() == null) continue;
            if (!a.getUser().getUsername().equalsIgnoreCase(username)) continue;

            if (emailedAppointmentIds.contains(a.getId())) continue;
            if (a.getSlot() == null || a.getSlot().getStartDateTime() == null) continue;

            LocalDateTime start = a.getSlot().getStartDateTime();
            if (!start.isAfter(now)) continue;

            Duration until = Duration.between(now, start);

            // Trigger when it enters the < 24 hour window
            if (until.compareTo(Duration.ofHours(24)) >= 0) continue;

            reminderService.send24hReminderIfNeeded(a); // ← هنا سيستدعى SmtpEmailSender إذا استخدمته
            emailedAppointmentIds.add(a.getId());
        }
    }
}