package MainApp;

import persistence.DataRepository;
import Service.AuthService;
import Service.BookingService;
import presentation.LoginFrame;
import domain.TimeSlot;
import domain.Category;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        DataRepository repo = new DataRepository();

        List<Category> categories = buildCategories();
        for (Category c : categories) {
            repo.addCategory(c);
        }

        seedTimeSlots(repo, categories, 14);

        AuthService authService = new AuthService(repo);
        BookingService bookingService = new BookingService(repo);

        javax.swing.SwingUtilities.invokeLater(() -> {
            new LoginFrame(authService, bookingService, repo).setVisible(true);
        });
    }

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