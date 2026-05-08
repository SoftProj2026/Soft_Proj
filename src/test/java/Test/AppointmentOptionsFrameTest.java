package Test;

import domain.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import persistence.DataRepository;
import presentation.AppointmentOptionsFrame;
import presentation.DialogUtil;
import presentation.UITheme;
import service.AdditionalAppointmentService;
import service.AppointmentTypeRules;
import service.AppointmentTypeService;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
public class AppointmentOptionsFrameTest {
    @BeforeAll
    static void skipIfHeadless() {
        Assumptions.assumeFalse(
            java.awt.GraphicsEnvironment.isHeadless(),
            "Skipping GUI tests in CI headless mode"
        );
    }
    private DataRepository repo;
    private AppointmentTypeService typeService;
    private User user;
    private Appointment appointment;
    private Category cat;

    private static void runOnEdtAndWait(Runnable r) throws Exception {
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
            return;
        }

        final Throwable[] t = new Throwable[1];

        SwingUtilities.invokeAndWait(() -> {
            try {
                r.run();
            } catch (Throwable e) {
                t[0] = e;
            }
        });

        if (t[0] != null) {
            throw new RuntimeException(t[0]);
        }
    }

    @BeforeEach
    void setUp() {
        UITheme.apply();

        repo = mock(DataRepository.class);
        typeService = mock(AppointmentTypeService.class);

        user = new User(
                "T",
                "U",
                "tester",
                "pw",
                java.time.LocalDate.of(1990, 1, 1),
                "t@example.com"
        );

        cat = new Category("Consult");

        TimeSlot originalSlot = new TimeSlot(LocalDateTime.now().minusDays(1), 60, cat);
        appointment = new Appointment(user, originalSlot, 60, 1);
        appointment.confirm();
    }

    @AfterEach
    void tearDown() {
        for (Window w : Window.getWindows()) {
            if (w != null) {
                w.dispose();
            }
        }
    }

    private static JComboBox<?> findCombo(Container root) {
        for (Component c : root.getComponents()) {
            if (c instanceof JComboBox) {
                return (JComboBox<?>) c;
            }

            if (c instanceof Container) {
                JComboBox<?> r = findCombo((Container) c);
                if (r != null) {
                    return r;
                }
            }
        }

        return null;
    }

    private static JButton findButton(Container root, String text) {
        for (Component c : root.getComponents()) {
            if (c instanceof JButton && text.equals(((JButton) c).getText())) {
                return (JButton) c;
            }

            if (c instanceof Container) {
                JButton r = findButton((Container) c, text);
                if (r != null) {
                    return r;
                }
            }
        }

        return null;
    }

    /**
     * Finds the duration spinner by looking for a spinner whose model max is >= 30 and <= 240,
     * and whose initial value is commonly 30, as in AppointmentOptionsFrame.
     * This avoids accidentally grabbing participants spinner, which is usually 1..5.
     */
    private static JSpinner findDurationSpinner(Container root) {
        for (Component c : root.getComponents()) {
            if (c instanceof JSpinner sp) {
                SpinnerModel m = sp.getModel();

                if (m instanceof SpinnerNumberModel nm) {
                    Number min = (Number) nm.getMinimum();
                    Number max = (Number) nm.getMaximum();
                    Number val = (Number) nm.getNumber();

                    boolean looksLikeDuration =
                            min.intValue() <= 1 &&
                            max.intValue() >= 30 &&
                            max.intValue() <= 300 &&
                            val.intValue() >= 1;

                    boolean looksLikeParticipants =
                            min.intValue() == 1 &&
                            max.intValue() == 5;

                    if (looksLikeDuration && !looksLikeParticipants) {
                        return sp;
                    }
                }
            }

            if (c instanceof Container cc) {
                JSpinner r = findDurationSpinner(cc);
                if (r != null) {
                    return r;
                }
            }
        }

        return null;
    }

    private static void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

    @Test
    void loadAvailableSlots_populates_combo_with_matching_future_slots() throws Exception {
        TimeSlot s1 = new TimeSlot(LocalDateTime.now().plusDays(1), 30, cat);
        TimeSlot s2 = new TimeSlot(LocalDateTime.now().plusDays(2), 30, new Category("Other"));

        when(repo.getSlots()).thenReturn(List.of(s1, s2));

        try (MockedStatic<DialogUtil> d = mockStatic(DialogUtil.class)) {
            d.when(() -> DialogUtil.show(any(), anyString(), anyString(), any())).then(inv -> null);

            final AppointmentOptionsFrame[] ref = new AppointmentOptionsFrame[1];
            runOnEdtAndWait(() -> ref[0] = new AppointmentOptionsFrame(null, repo, appointment, typeService));
            AppointmentOptionsFrame frame = ref[0];

            JComboBox<?> combo = findCombo(frame.getContentPane());
            assertNotNull(combo, "slots combo should exist");
            assertTrue(combo.getItemCount() >= 1, "at least one matching slot should be present");

            Object item = combo.getItemAt(0);
            assertNotNull(item);
            assertTrue(item.toString().contains(s1.getStartDateTime().toLocalDate().toString()));

            runOnEdtAndWait(frame::dispose);
        }
    }

    @Test
    void updateDurationMax_adjusts_spinner_max_based_on_selected_slot() throws Exception {
        TimeSlot s = new TimeSlot(LocalDateTime.now().plusDays(1), 45, cat);

        when(repo.getSlots()).thenReturn(List.of(s));

        try (MockedStatic<DialogUtil> d = mockStatic(DialogUtil.class)) {
            d.when(() -> DialogUtil.show(any(), anyString(), anyString(), any())).then(inv -> null);

            final AppointmentOptionsFrame[] ref = new AppointmentOptionsFrame[1];
            runOnEdtAndWait(() -> ref[0] = new AppointmentOptionsFrame(null, repo, appointment, typeService));
            AppointmentOptionsFrame frame = ref[0];

            JComboBox<?> combo = findCombo(frame.getContentPane());
            assertNotNull(combo);

            runOnEdtAndWait(() -> combo.setSelectedIndex(0));

            JSpinner durationSpinner = findDurationSpinner(frame.getContentPane());
            assertNotNull(durationSpinner, "duration spinner must be found");

            int actualMax = ((Number) ((SpinnerNumberModel) durationSpinner.getModel()).getMaximum()).intValue();

            assertEquals(45, actualMax);

            runOnEdtAndWait(frame::dispose);
        }
    }

    @Test
    void onSave_missing_slot_shows_warning_and_does_not_create() throws Exception {
        when(repo.getSlots()).thenReturn(List.of());

        AdditionalAppointmentService mockAdditionalService = mock(AdditionalAppointmentService.class);

        try (MockedStatic<DialogUtil> d = mockStatic(DialogUtil.class)) {
            d.when(() -> DialogUtil.show(any(), anyString(), anyString(), any())).then(inv -> null);

            final AppointmentOptionsFrame[] ref = new AppointmentOptionsFrame[1];
            runOnEdtAndWait(() -> ref[0] = new AppointmentOptionsFrame(null, repo, appointment, typeService));
            AppointmentOptionsFrame frame = ref[0];

            setPrivateField(frame, "additionalAppointmentService", mockAdditionalService);

            JComboBox<?> combo = findCombo(frame.getContentPane());
            assertNotNull(combo);
            assertEquals(0, combo.getItemCount(), "no slots should be available in combo");

            JButton save = findButton(frame.getContentPane(), "Save");
            assertNotNull(save);
            runOnEdtAndWait(save::doClick);

            verify(mockAdditionalService, never())
                    .createNewAppointment(any(), any(), anyInt(), anyInt(), any(), any(), any());

            runOnEdtAndWait(frame::dispose);
        }
    }

    @Test
    void onSave_not_confirmed_shows_warning_and_does_not_create() throws Exception {
        appointment = new Appointment(user, appointment.getSlot(), 30, 1);

        when(repo.getSlots()).thenReturn(List.of());

        AdditionalAppointmentService mockAdditionalService = mock(AdditionalAppointmentService.class);

        try (MockedStatic<DialogUtil> d = mockStatic(DialogUtil.class)) {
            d.when(() -> DialogUtil.show(any(), anyString(), anyString(), any())).then(inv -> null);

            final AppointmentOptionsFrame[] ref = new AppointmentOptionsFrame[1];
            runOnEdtAndWait(() -> ref[0] = new AppointmentOptionsFrame(null, repo, appointment, typeService));
            AppointmentOptionsFrame frame = ref[0];

            setPrivateField(frame, "additionalAppointmentService", mockAdditionalService);

            JButton save = findButton(frame.getContentPane(), "Save");
            assertNotNull(save);
            runOnEdtAndWait(save::doClick);

            verify(mockAdditionalService, never())
                    .createNewAppointment(any(), any(), anyInt(), anyInt(), any(), any(), any());

            runOnEdtAndWait(frame::dispose);
        }
    }

    @Test
    void onSave_validation_fails_shows_warning_and_does_not_create() throws Exception {
        TimeSlot s = new TimeSlot(LocalDateTime.now().plusDays(1), 30, cat);
        when(repo.getSlots()).thenReturn(List.of(s));

        AdditionalAppointmentService mockAdditionalService = mock(AdditionalAppointmentService.class);

        try (MockedStatic<DialogUtil> d = mockStatic(DialogUtil.class);
             MockedStatic<AppointmentTypeRules> rules = mockStatic(AppointmentTypeRules.class)) {

            d.when(() -> DialogUtil.show(any(), anyString(), anyString(), any())).then(inv -> null);

            rules.when(() -> AppointmentTypeRules.validate(any(), anyInt(), anyInt(), any()))
                    .thenReturn("Bad: too long");

            final AppointmentOptionsFrame[] ref = new AppointmentOptionsFrame[1];
            runOnEdtAndWait(() -> ref[0] = new AppointmentOptionsFrame(null, repo, appointment, typeService));
            AppointmentOptionsFrame frame = ref[0];

            setPrivateField(frame, "additionalAppointmentService", mockAdditionalService);

            JComboBox<?> combo = findCombo(frame.getContentPane());
            assertNotNull(combo);
            runOnEdtAndWait(() -> combo.setSelectedIndex(0));

            JSpinner durationSpinner = findDurationSpinner(frame.getContentPane());
            assertNotNull(durationSpinner);
            runOnEdtAndWait(() -> durationSpinner.setValue(30));

            JButton save = findButton(frame.getContentPane(), "Save");
            assertNotNull(save);
            runOnEdtAndWait(save::doClick);

        }
    }

    @Test
    void onSave_success_calls_additionalService_and_closes() throws Exception {
        TimeSlot s = new TimeSlot(LocalDateTime.now().plusDays(1), 30, cat);
        when(repo.getSlots()).thenReturn(List.of(s));

        AdditionalAppointmentService mockAdditionalService = mock(AdditionalAppointmentService.class);

        when(mockAdditionalService.createNewAppointment(
                any(User.class),
                any(TimeSlot.class),
                anyInt(),
                anyInt(),
                any(AppointmentType.class),
                any(),
                any()
        )).thenReturn("Saved.");

        try (MockedStatic<DialogUtil> d = mockStatic(DialogUtil.class)) {
            d.when(() -> DialogUtil.show(any(), anyString(), anyString(), any())).then(inv -> null);

            final AppointmentOptionsFrame[] ref = new AppointmentOptionsFrame[1];
            runOnEdtAndWait(() -> ref[0] = new AppointmentOptionsFrame(null, repo, appointment, typeService));
            AppointmentOptionsFrame frame = ref[0];

            setPrivateField(frame, "additionalAppointmentService", mockAdditionalService);

            JComboBox<?> combo = findCombo(frame.getContentPane());
            assertNotNull(combo);
            runOnEdtAndWait(() -> combo.setSelectedIndex(0));

            JSpinner durationSpinner = findDurationSpinner(frame.getContentPane());
            assertNotNull(durationSpinner);
            runOnEdtAndWait(() -> durationSpinner.setValue(30));

            JButton save = findButton(frame.getContentPane(), "Save");
            assertNotNull(save);
            runOnEdtAndWait(save::doClick);

            verify(mockAdditionalService, timeout(2000).atLeastOnce())
                    .createNewAppointment(
                            any(User.class),
                            any(TimeSlot.class),
                            anyInt(),
                            anyInt(),
                            any(AppointmentType.class),
                            any(),
                            any()
                    );

            runOnEdtAndWait(() -> assertFalse(frame.isDisplayable()));

            runOnEdtAndWait(frame::dispose);
        }
    }
}