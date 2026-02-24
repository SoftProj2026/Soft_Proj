package MainApp;

import persistence.DataRepository;
import Service.AuthService;
import Service.BookingService;
import presentation.LoginFrame;

public class Main {

    public static void main(String[] args) {

        DataRepository repo = new DataRepository();

        AuthService authService = new AuthService(repo);
        BookingService bookingService = new BookingService(repo);

        javax.swing.SwingUtilities.invokeLater(() -> {
            new LoginFrame(authService, bookingService, repo)
                    .setVisible(true);
        });
    }
}