package service;

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
 * Periodically checks upcoming confirmed appointments for the currently logged-in user and triggers reminder emails.
 *
 * <p>Reminder rules applied by the scheduler:</p>
 * <ul>
 *   <li>Only {@link AppointmentStatus#CONFIRMED} appointments are considered.</li>
 *   <li>Only appointments belonging to the currently logged-in user are considered.</li>
 *   <li>Only future appointments are considered.</li>
 *   <li>Reminders are triggered only when the appointment start is within the next 24 hours.</li>
 * </ul>
 *
 * <p>To avoid spamming, the scheduler tracks already-reminded appointment IDs in-memory and will not send
 * multiple reminders for the same appointment during the same application run.</p>
 * @author Qussai
 * @version 1.0
 */
public class EmailReminderScheduler {

    private final DataRepository repo;
    private final AuthService auth;
    private final BookingEmailReminderService reminderService;

    private final ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
    private final Set<Integer> emailedAppointmentIds = new HashSet<>();

    private final long checkEveryMinutes;
    private final Clock clock;

    /**
     * Creates a scheduler using the system default clock.
     *
     * @param repo             repository used to read appointments
     * @param auth             authentication service used to determine the current logged-in user
     * @param reminderService  reminder service used to send reminder emails
     * @param checkEveryMinutes interval in minutes between checks
     */
    public EmailReminderScheduler(
            DataRepository repo,
            AuthService auth,
            BookingEmailReminderService reminderService,
            long checkEveryMinutes
    ) {
        this(repo, auth, reminderService, checkEveryMinutes, Clock.systemDefaultZone());
    }

    /**
     * Creates a scheduler.
     *
     * @param repo             repository used to read appointments
     * @param auth             authentication service used to determine the current logged-in user
     * @param reminderService  reminder service used to send reminder emails
     * @param checkEveryMinutes interval in minutes between checks
     * @param clock            clock used for time computations (useful for testing)
     */
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

    /**
     * Starts the periodic reminder check.
     */
    public void start() {
        exec.scheduleAtFixedRate(this::safeCheck, 0, checkEveryMinutes, TimeUnit.MINUTES);
    }

    /**
     * Stops the scheduler and clears in-memory tracking state.
     */
    public void stop() {
        exec.shutdownNow();
        emailedAppointmentIds.clear();
    }

    /**
     * Executes a reminder check safely, preventing scheduler thread crashes.
     */
    private void safeCheck() {
        try {
            checkAndSend();
        } catch (Exception ex) {
            System.out.println("[EmailReminderScheduler] ERROR: " + ex.getMessage());
        }
    }

    /**
     * Performs one check cycle and triggers reminders when eligible.
     */
    private void checkAndSend() {
        boolean loggedIn = (auth != null && auth.isLoggedIn() && auth.getCurrentUser() != null);
        if (!loggedIn) return;

        String username = auth.getCurrentUser().getUsername();
        LocalDateTime now = LocalDateTime.now(clock);

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
            if (until.compareTo(Duration.ofHours(24)) > 0) continue;

            reminderService.send24hReminderIfNeeded(a);
            emailedAppointmentIds.add(a.getId());
        }
    }
}