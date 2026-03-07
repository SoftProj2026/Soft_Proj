package MainApp;

import Service.AuthService;
import Service.BookingRequestService;
import Service.BookingService;
import domain.Administrator;
import domain.Category;
import domain.Provider;
import domain.TimeSlot;
import persistence.DataRepository;
import persistence.RepoStorage;
import presentation.LoginFrame;
import presentation.UITheme;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Application entry point.
 *
 * <p>This class bootstraps the application by applying the UI theme, loading persisted repository data,
 * seeding initial data on first run, and launching the Swing login window.</p>
 */
public class Main {

    /**
     * Starts the application.
     *
     * <p>Startup flow:</p>
     * <ul>
     *   <li>Apply the UI theme.</li>
     *   <li>Load repository data from disk (or create a new repository if not found).</li>
     *   <li>If the repository looks empty, seed initial accounts, categories, and time slots.</li>
     *   <li>Create core services and open the {@link LoginFrame}.</li>
     * </ul>
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        UITheme.apply();

        DataRepository repo = RepoStorage.loadOrNew();

        boolean looksEmpty = repo.getUsers().isEmpty()
                && repo.getProviders().isEmpty()
                && repo.getCategories().isEmpty()
                && repo.getSlots().isEmpty()
                && repo.getAppointments().isEmpty()
                && repo.getBookingRequests().isEmpty()
                && repo.getContactRequests().isEmpty()
                && repo.getAuditEvents().isEmpty();

        if (looksEmpty) {
            repo.addUser(new Administrator("admin", "Admin@123"));

            repo.addProvider(new Provider(
                    "qrbooking",
                    "Comp@1234",
                    "QR Booking",
                    "",
                    "remaajomaa842@gmail.com",
                    ""
            ));

            List<Category> categories = buildCategories();
            for (Category c : categories) {
                repo.addCategory(c);
            }

            seedCategoryAdmins(repo, categories);
            seedTimeSlots(repo, categories, 7);

            RepoStorage.save(repo);
        }

        AuthService authService = new AuthService(repo);
        BookingService bookingService = new BookingService(repo);

        new BookingRequestService(repo);

        javax.swing.SwingUtilities.invokeLater(() -> {
            new LoginFrame(authService, bookingService, repo).setVisible(true);
        });
    }

    /**
     * Seeds one administrator account per category using a deterministic username derived from the
     * category name.
     *
     * @param repo       repository to insert users into
     * @param categories categories used to derive category-admin usernames
     */
    private static void seedCategoryAdmins(DataRepository repo, List<Category> categories) {
        String pass = "Admin@123";
        for (Category c : categories) {
            String u = BookingRequestService.categoryAdminUsername(c);
            repo.addUser(new domain.Administrator(u, pass));
        }
    }

    /**
     * Builds the list of predefined categories used by the application.
     *
     * @return list of categories
     */
    private static List<Category> buildCategories() {
        List<Category> categories = new ArrayList<>();

        categories.add(new Category("Bus Reservation"));
        categories.add(new Category("Driver Service"));
        categories.add(new Category("Airport Pickup"));
        categories.add(new Category("Delivery Vehicle"));

        categories.add(new Category("Conference Hall"));
        categories.add(new Category("Training Room"));
        categories.add(new Category("Shared Workspace"));
        categories.add(new Category("Equipment Rental (Projector, Laptop)"));

        categories.add(new Category("Wedding Hall"));
        categories.add(new Category("Birthday Venue"));
        categories.add(new Category("Photography Studio"));
        categories.add(new Category("Event Planner Meeting"));

        categories.add(new Category("Doctor Appointment"));
        categories.add(new Category("Legal Consultation"));
        categories.add(new Category("Private Tutor"));
        categories.add(new Category("Gym Session"));

        categories.add(new Category("Lab Reservation"));
        categories.add(new Category("Library Study Room"));
        categories.add(new Category("Exam Hall"));
        categories.add(new Category("Academic Advisor Meeting"));

        return categories;
    }

    /**
     * Seeds time slots for each category for a given number of days ahead.
     *
     * <p>Slots are generated as 1-hour intervals from 09:00 to 16:00 (inclusive start times).
     * Fridays are skipped.</p>
     *
     * @param repo       repository to insert slots into
     * @param categories categories to create slots for
     * @param daysAhead  number of days to generate slots for starting from today
     */
    private static void seedTimeSlots(DataRepository repo, List<Category> categories, int daysAhead) {
        int durationMinutes = 60;

        LocalTime start = LocalTime.of(9, 0);
        LocalTime lastStart = LocalTime.of(16, 0);

        LocalDate today = LocalDate.now();

        for (int d = 0; d < daysAhead; d++) {
            LocalDate date = today.plusDays(d);
            DayOfWeek dow = date.getDayOfWeek();

            if (dow == DayOfWeek.FRIDAY) {
                continue;
            }

            for (Category c : categories) {
                LocalTime t = start;

                while (!t.isAfter(lastStart)) {
                    LocalDateTime dateTime = LocalDateTime.of(date, t);
                    repo.addSlot(new TimeSlot(dateTime, durationMinutes, c));
                    t = t.plusHours(1);
                }
            }
        }
    }
}