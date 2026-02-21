package MainApp;

import persistence.DataRepository;
import Service.AuthService;
import Service.ScheduleService;
import Service.BookingService;
import presentation.LoginFrame;
import domain.TimeSlot;

import java.time.LocalDateTime;
 
public class Main {

    public static void main(String[] args) {

        DataRepository repo = new DataRepository();

        repo.addSlot(new TimeSlot(LocalDateTime.now().plusHours(2), 60));
        repo.addSlot(new TimeSlot(LocalDateTime.now().plusDays(1), 60));
        AuthService authService = new AuthService(repo);
        ScheduleService scheduleService = new ScheduleService(repo, authService);

        BookingService bookingService = new BookingService(repo);

        javax.swing.SwingUtilities.invokeLater(() -> {
        	new LoginFrame(authService, bookingService).setVisible(true);                    
        });
    }
}