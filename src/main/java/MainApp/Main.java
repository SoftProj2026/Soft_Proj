package MainApp;

import Service.AuthService;
import Service.BookingRequestService;
import Service.BookingService;
import domain.Administrator;
import domain.Category;
import domain.Provider;
import domain.TimeSlot;
import persistence.DataRepository;
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
 * <p>
 * Bootstraps the Swing UI application by:
 * </p>
 * <ol>
 *   <li>Applying the UI theme</li>
 *   <li>Creating the in-memory {@link DataRepository}</li>
 *   <li>Seeding a single provider company ("QR Booking")</li>
 *   <li>Seeding categories and category-admin accounts</li>
 *   <li>Printing category-admin credentials to the console</li>
 *   <li>Seeding time slots for the upcoming days</li>
 *   <li>Creating core services and launching the {@link LoginFrame}</li>
 * </ol>
 */
public class Main {

    /**
     * Starts the application.
     *
     * @param args command line args (not used)
     */
    public static void main(String[] args) {

        UITheme.apply();

        DataRepository repo = new DataRepository();

        repo.addUser(new Administrator("admin", "Admin@123"));

        repo.addProvider(new Provider(
                "qrbooking",
                "Comp@1234",
                "QR Booking",
                "+0000000000",
                "contact@qrbooking.example",
                "N/A"
        ));

        List<Category> categories = buildCategories();
        for (Category c : categories) {
            repo.addCategory(c);
        }

        String categoryAdminPassword = seedCategoryAdmins(repo, categories);

        printCategoryAdminCredentials(categories, categoryAdminPassword);

        seedTimeSlots(repo, categories, 7);

        AuthService authService = new AuthService(repo);
        BookingService bookingService = new BookingService(repo);

        new BookingRequestService(repo);

        javax.swing.SwingUtilities.invokeLater(() -> {
            new LoginFrame(authService, bookingService, repo).setVisible(true);
        });
    }

    /**
     * Seeds category-admin accounts for each category.
     *
     * @param repo       repository to populate
     * @param categories list of categories
     * @return the password used for all seeded category admins
     */
    private static String seedCategoryAdmins(DataRepository repo, List<Category> categories) {
        String pass = "Admin@123";
        for (Category c : categories) {
            String u = BookingRequestService.categoryAdminUsername(c);
            repo.addUser(new Administrator(u, pass));
        }
        return pass;
    }

    /**
     * Prints category-admin credentials to the console.
     *
     * @param categories list of categories
     * @param password   shared password used for category admins
     */
    private static void printCategoryAdminCredentials(List<Category> categories, String password) {
        System.out.println("=== Category Admin Credentials ===");
        System.out.println("Password (all category admins): " + password);
        System.out.println("---------------------------------");
        for (Category c : categories) {
            String username = BookingRequestService.categoryAdminUsername(c);
            System.out.println("Category: " + c.getName() + " | Username: " + username + " | Password: " + password);
        }
        System.out.println("=================================");
    }

    /**
     * Builds the list of booking categories shown in the UI.
     *
     * @return list of default categories
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
     * Seeds the repository with hourly time slots for each category.
     * <p>
     * Default behavior:
     * </p>
     * <ul>
     *   <li>Creates 1-hour slots from 09:00 to 16:00 inclusive</li>
     *   <li>Skips Fridays</li>
     *   <li>Seeds for {@code daysAhead} days starting from today</li>
     * </ul>
     *
     * @param repo       repository to populate
     * @param categories categories to create slots for
     * @param daysAhead  number of days to generate starting from today
     */
    private static void seedTimeSlots(DataRepository repo,
                                      List<Category> categories,
                                      int daysAhead) {

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