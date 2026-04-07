package Test;

import Service.AiBookingAssistantService;
import Service.AuthService;
import Service.BookingResult;
import Service.BookingService;
import domain.Category;
import domain.TimeSlot;
import domain.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import persistence.DataRepository;
import presentation.BookingTypeChoiceDialog;
import presentation.UITheme;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


class BookingTypeChoiceDialogAiFlowTest {

    private DataRepository repo;
    private AuthService auth;
    private BookingService booking;
    private Category cat;
    private User user;

    private MockedStatic<JOptionPane> jops;

    private static void runOnEdtAndWait(Runnable r) throws Exception {
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
            return;
        }
        final Throwable[] err = new Throwable[1];
        SwingUtilities.invokeAndWait(() -> {
            try { r.run(); } catch (Throwable t) { err[0] = t; }
        });
        if (err[0] != null) throw new RuntimeException(err[0]);
    }

    @BeforeEach
    void setUp() {
        UITheme.apply();

        repo = mock(DataRepository.class);
        auth = mock(AuthService.class);
        booking = mock(BookingService.class);

        user = new User("T", "U", "tester", "pw", java.time.LocalDate.of(1990,1,1), "t@example.com");
        when(auth.isLoggedIn()).thenReturn(true);
        when(auth.getCurrentUser()).thenReturn(user);

        cat = new Category("Photography Studio");

        jops = mockStatic(JOptionPane.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (jops != null) jops.close();

        runOnEdtAndWait(() -> {
            for (Window w : Window.getWindows()) {
                if (w != null && w.isDisplayable()) w.dispose();
            }
        });
    }

    private JButton findButton(Container root, String text) {
        for (Component c : root.getComponents()) {
            if (c instanceof JButton && text.equals(((JButton) c).getText())) return (JButton) c;
            if (c instanceof Container) {
                JButton b = findButton((Container) c, text);
                if (b != null) return b;
            }
        }
        return null;
    }

    @Test
    void aiBooking_button_runs_flow_and_sends_request_successfully() throws Exception {
        TimeSlot s1 = new TimeSlot(LocalDateTime.now().plusDays(1).withHour(9).withMinute(0), 60, cat);
        TimeSlot s2 = new TimeSlot(LocalDateTime.now().plusDays(1).withHour(10).withMinute(0), 60, cat);
        TimeSlot s3 = new TimeSlot(LocalDateTime.now().plusDays(1).withHour(11).withMinute(0), 60, cat);
        TimeSlot s4 = new TimeSlot(LocalDateTime.now().plusDays(1).withHour(14).withMinute(0), 60, cat);
        TimeSlot s5 = new TimeSlot(LocalDateTime.now().plusDays(1).withHour(15).withMinute(0), 60, cat);

        List<TimeSlot> suggestions = List.of(s1, s2, s3, s4, s5);

        BookingResult ok = mock(BookingResult.class);
        when(ok.isSuccess()).thenReturn(true);
        when(ok.getMessage()).thenReturn("Request submitted successfully.");

        jops.when(() -> JOptionPane.showInputDialog(any(Component.class), any(), any()))
                .thenReturn("2", "30");

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String choice1 = "1) " + s1.getStartDateTime().format(fmt) + " → " + s1.getEndDateTime().format(fmt);
        jops.when(() -> JOptionPane.showInputDialog(
                        any(Component.class),
                        any(),
                        anyString(),
                        anyInt(),
                        any(),
                        any(Object[].class),
                        any()
                ))
                .thenReturn(choice1);

        jops.when(() -> JOptionPane.showMessageDialog(
                        any(Component.class),
                        any(),
                        anyString(),
                        anyInt()
                ))
                .thenAnswer(inv -> null);

        try (MockedConstruction<AiBookingAssistantService> mocked =
                     mockConstruction(AiBookingAssistantService.class, (mock, ctx) -> {
                         when(mock.suggestTopMutualSlots(any(User.class), any(Category.class), eq(5)))
                                 .thenReturn(suggestions);

                         when(mock.sendRequestForSlot(any(User.class), any(TimeSlot.class), anyInt(), anyInt()))
                                 .thenReturn(ok);
                     })) {

            final BookingTypeChoiceDialog[] ref = new BookingTypeChoiceDialog[1];
            runOnEdtAndWait(() -> ref[0] = new BookingTypeChoiceDialog(null, cat, repo, auth, booking));
            BookingTypeChoiceDialog dlg = ref[0];

            JButton aiBtn = findButton(dlg.getContentPane(), "AI Booking (Suggest 5 slots + Send Request)");
            assertNotNull(aiBtn, "AI button must exist");

            runOnEdtAndWait(aiBtn::doClick);

            assertEquals(1, mocked.constructed().size(), "Expected AiBookingAssistantService to be constructed exactly once");

            AiBookingAssistantService aiMock = mocked.constructed().get(0);

            verify(aiMock, atLeastOnce()).suggestTopMutualSlots(eq(user), eq(cat), eq(5));
            verify(aiMock, atLeastOnce()).sendRequestForSlot(eq(user), any(TimeSlot.class), eq(30), eq(2));

            runOnEdtAndWait(() -> assertFalse(dlg.isDisplayable(), "Dialog should be disposed after success"));
        }
    }
}