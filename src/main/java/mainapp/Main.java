package mainapp;

import Service.AuthService;
import Service.BookingRequestService;
import Service.BookingService;
import domain.Administrator;
import domain.Category;
import domain.Provider;
import domain.TimeSlot;
import domain.User;
import persistence.DataRepository;
import persistence.RepoStorage;
import presentation.LoginFrame;
import presentation.UITheme;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Application entry point.
 *
 * <p>This class bootstraps the application by applying the UI theme, loading persisted repository data,
 * purging removed categories from existing saved data, seeding initial data on first run, and launching
 * the Swing login window.</p>
 *
 * @author Qussaialaw & s12219530-cpu (remaa)
 * @version 1.0
 */
public class Main {

    public static void main(String[] args) {
        UITheme.apply();

        DataRepository repo = RepoStorage.loadOrNew();

        ensureBigAdminAndProviderExist(repo);

        purgeRemovedCategories(repo);

        if (repo.getCategories() == null || repo.getCategories().isEmpty()
                || (repo.getCategories().size() == 1
                && repo.getCategories().get(0) != null
                && "Keep This".equalsIgnoreCase(repo.getCategories().get(0).getName()))) {
            repo.getCategories().clear();
            List<Category> categories = buildCategories();
            for (Category c : categories) {
                repo.addCategory(c);
            }

            seedCategoryAdmins(repo, categories);
            seedTimeSlots(repo, categories, 7);

            RepoStorage.save(repo);
        }
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

        javax.swing.SwingUtilities.invokeLater(() ->
                new LoginFrame(authService, bookingService, repo).setVisible(true)
        );
    }

    /**
     * Ensures that the big-admin account ("admin") and the default company provider ("qrbooking")
     * exist in the repository. This fixes cases where old saved data is not empty but missing
     * the admin user, causing QR admin login to fail with:
     * "Admin account not found in repository."
     *
     * @param repo repository to patch if missing required accounts
     */
    private static void ensureBigAdminAndProviderExist(DataRepository repo) {
        if (repo == null) return;

        boolean hasAdmin = false;
        for (User u : repo.getUsers()) {
            if (u != null && u.getUsername() != null && u.getUsername().equalsIgnoreCase("admin")) {
                hasAdmin = true;
                break;
            }
        }

        if (!hasAdmin) {
            repo.addUser(new Administrator("admin", "Admin@123"));
        }

        boolean hasProvider = false;
        for (Provider p : repo.getProviders()) {
            if (p != null && p.getUsername() != null && p.getUsername().equalsIgnoreCase("qrbooking")) {
                hasProvider = true;
                break;
            }
        }

        if (!hasProvider) {
            repo.addProvider(new Provider(
                    "qrbooking",
                    "Comp@1234",
                    "QR Booking",
                    "",
                    "remaajomaa842@gmail.com",
                    ""
            ));
        }

        // Save immediately so next run is consistent
        RepoStorage.save(repo);
    }

    /**
     * Purges removed categories from the repository (categories, slots, appointments, requests,
     * category-admin users, cancel tracking, and audit events).
     *
     * @param repo repository to purge
     */
    private static void purgeRemovedCategories(DataRepository repo) {
        if (repo == null) {
            return;
        }

        Set<String> toRemove = new HashSet<>();
        toRemove.add("Doctor Appointment");
        toRemove.add("Airport Pickup");
        toRemove.add("Private Tutor");
        toRemove.add("Driver Service");
        toRemove.add("Academic Advisor Meeting");
        toRemove.add("Equipment Rental (Projector, Laptop)");
        toRemove.add("Shared Workspace");
        toRemove.add("Event Planner Meeting");
        toRemove.add("Bus Reservation");
        toRemove.add("Delivery Vehicle");
        toRemove.add("Gym Session");
        toRemove.add("Exam Hall");
        toRemove.add("Lab Reservation");
        toRemove.add("Library Study Room");

        int removed = repo.purgeCategories(toRemove);
        if (removed > 0) {
            RepoStorage.save(repo);
        }
    }

    /**
     * Seeds one administrator account per category using a deterministic username derived from the category name.
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

        categories.add(new Category("Conference Hall"));
        categories.add(new Category("Training Room"));

        categories.add(new Category("Wedding Hall"));
        categories.add(new Category("Birthday Venue"));
        categories.add(new Category("Photography Studio"));

        categories.add(new Category("Legal Consultation"));

        categories.add(new Category("Apartment for rent"));
        categories.add(new Category("Car for rent"));
        categories.add(new Category("Meeting with a building contractor"));

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