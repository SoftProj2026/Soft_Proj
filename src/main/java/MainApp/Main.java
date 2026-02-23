package MainApp;

import persistence.DataRepository;
import Service.AuthService;
import Service.BookingService;
import presentation.LoginFrame;

/**
 * Entry point of the appointment booking application.
 *
 * <p>Bootstraps the application by creating the shared {@link DataRepository},
 * the {@link AuthService} and {@link BookingService}, then launches the
 * {@link LoginFrame} on the Swing Event Dispatch Thread.</p>
 */
public class Main {

    /**
     * Application entry point.
     *
     * @param args command-line arguments (not used)
     */
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