/*package MainApp;

import Service.AuthService;
import Service.BookingService;
import domain.Category;
import domain.TimeSlot;
import persistence.DataRepository;
import presentation.LoginFrame;

import java.time.LocalDateTime;

public class Main {

    public static void main(String[] args) {

        DataRepository repo = new DataRepository();

        // Categories
        Category wedding = new Category("Wedding Hall");
        Category birthday = new Category("Birthday Venue");
        Category privateTutor = new Category("Private Tutor");

        repo.addCategory(wedding);
        repo.addCategory(birthday);
        repo.addCategory(privateTutor);

        // Slots جاهزة (بكرا من 10 إلى 3 مثلاً)
        LocalDateTime base = LocalDateTime.now()
                .plusDays(1)
                .withHour(10).withMinute(0).withSecond(0).withNano(0);

        for (int i = 0; i < 6; i++) {
            repo.addSlot(new TimeSlot(base.plusHours(i), 60, wedding));
            repo.addSlot(new TimeSlot(base.plusHours(i), 60, birthday));
            repo.addSlot(new TimeSlot(base.plusHours(i), 60, privateTutor));
        }

        AuthService authService = new AuthService(repo);
        BookingService bookingService = new BookingService(repo);

        javax.swing.SwingUtilities.invokeLater(() -> {
            new LoginFrame(authService, bookingService, repo).setVisible(true);
        });
    }
}*/
package MainApp;

import persistence.DataRepository;
import Service.AuthService;
import Service.BookingService;
import Service.ScheduleService;
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

        // 1) Seed categories (كما أرسلت)
        List<Category> categories = buildCategories();
        for (Category c : categories) {
            repo.addCategory(c);
        }

        // 2) Seed slots (جاهزة للحجز)
        seedTimeSlots(repo, categories, 14);

        AuthService authService = new AuthService(repo);
        BookingService bookingService = new BookingService(repo);
        ScheduleService scheduleService = new ScheduleService(repo, authService); // إذا ستستخدمه لاحقًا

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
        LocalTime lastStart = LocalTime.of(16, 0); // آخر موعد يبدأ 16:00 وينتهي 17:00

        LocalDate today = LocalDate.now();

        for (int d = 0; d < daysAhead; d++) {

            LocalDate date = today.plusDays(d);
            DayOfWeek dow = date.getDayOfWeek();

            // أنت كاتب بدك تستثني الجمعة بالكامل
            if (dow == DayOfWeek.FRIDAY) {
                continue;
            }

            for (Category c : categories) {

                LocalTime t = start;

                while (!t.isAfter(lastStart)) {
                    LocalDateTime dateTime = LocalDateTime.of(date, t);

                    // مهم: هذا يعتمد على TimeSlot constructor (LocalDateTime, int, Category)
                    repo.addSlot(new TimeSlot(dateTime, durationMinutes, c));

                    t = t.plusHours(1);
                }
            }
        }
    }
}