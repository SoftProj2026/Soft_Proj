
package MainApp;

import persistence.DataRepository;
import Service.AuthService;
import Service.BookingService;
import Service.ScheduleService;
import presentation.LoginFrame;
import domain.TimeSlot;
import domain.Category;

import java.time.LocalDateTime;

public class Main {

    public static void main(String[] args) {

        DataRepository repo = new DataRepository();

        Category car = new Category("Car Booking");
        Category land = new Category("Land Reservation");
        Category room = new Category("Meeting Room");

        repo.addCategory(car);
        repo.addCategory(land);
        repo.addCategory(room);

        repo.addSlot(new TimeSlot(LocalDateTime.now().plusHours(2), 60, car));
        repo.addSlot(new TimeSlot(LocalDateTime.now().plusDays(1), 60, land));
        repo.addSlot(new TimeSlot(LocalDateTime.now().plusDays(2), 60, room));

        AuthService authService = new AuthService(repo);
        BookingService bookingService = new BookingService(repo);
        ScheduleService scheduleService = new ScheduleService(repo, authService);

        javax.swing.SwingUtilities.invokeLater(() -> {
            new LoginFrame(authService, bookingService, repo).setVisible(true);
        });
    }
}