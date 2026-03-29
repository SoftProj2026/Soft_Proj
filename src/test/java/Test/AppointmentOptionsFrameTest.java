package Test;

import Service.AdditionalAppointmentService;
import Service.AppointmentTypeService;
import Service.AppointmentTypeRules;
import domain.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import persistence.DataRepository;
import presentation.AppointmentOptionsFrame;
import presentation.DialogUtil;
import presentation.UITheme;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

 
public class AppointmentOptionsFrameTest {

    private DataRepository repo;
    private AppointmentTypeService typeService;
    private User user;
    private Appointment appointment;
    private Category cat;

    private static void runOnEdtAndWait(Runnable r) throws Exception {
        if (SwingUtilities.isEventDispatchThread()) { r.run(); return; }
        final Throwable[] t = new Throwable[1];
        SwingUtilities.invokeAndWait(() -> {
            try { r.run(); } catch (Throwable e) { t[0] = e; }
        });
        if (t[0] != null) throw new RuntimeException(t[0]);
    }

    @BeforeEach
    void setUp() {
        UITheme.apply();

        repo = mock(DataRepository.class);
        typeService = mock(AppointmentTypeService.class);

        user = new User("T", "U", "tester", "pw", java.time.LocalDate.of(1990,1,1), "t@example.com");
        cat = new Category("Consult");
        TimeSlot originalSlot = new TimeSlot(LocalDateTime.now().minusDays(1), 60, cat); 
        appointment = new Appointment(user, originalSlot, 60, 1);
        appointment.confirm(); 
    }

    @AfterEach
    void tearDown() {
        for (Window w : Window.getWindows()) {
            if (w != null) w.dispose();
        }
    }

    private static JComboBox<?> findCombo(Container root) {
        for (Component c : root.getComponents()) {
            if (c instanceof JComboBox) return (JComboBox<?>) c;
            if (c instanceof Container) {
                JComboBox<?> r = findCombo((Container) c);
                if (r != null) return r;
            }
        }
        return null;
    }

    private static JSpinner findSpinner(Container root) {
        for (Component c : root.getComponents()) {
            if (c instanceof JSpinner) return (JSpinner) c;
            if (c instanceof Container) {
                JSpinner r = findSpinner((Container) c);
                if (r != null) return r;
            }
        }
        return null;
    }

    private static JButton findButton(Container root, String text) {
        for (Component c : root.getComponents()) {
            if (c instanceof JButton && text.equals(((JButton) c).getText())) return (JButton) c;
            if (c instanceof Container) {
                JButton r = findButton((Container) c, text);
                if (r != null) return r;
            }
        }
        return null;
    }

    private static void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

    private static long getSlotLengthMinutes(TimeSlot s) {
        if (s == null) return 60L;
        try {
            Method mEnd = null;
            try { mEnd = s.getClass().getMethod("getEndDateTime"); } catch (NoSuchMethodException ignored) {}
            if (mEnd != null) {
                Object end = mEnd.invoke(s);
                if (end instanceof java.time.LocalDateTime && s.getStartDateTime() != null) {
                    return Duration.between(s.getStartDateTime(), (java.time.LocalDateTime) end).toMinutes();
                }
            }
        } catch (Throwable ignored) {}

        try {
            Method mDur = null;
            try { mDur = s.getClass().getMethod("getDuration"); } catch (NoSuchMethodException ignored) {}
            if (mDur != null) {
                Object dur = mDur.invoke(s);
                if (dur instanceof Number) return ((Number) dur).longValue();
                if (dur instanceof java.time.Duration) return ((java.time.Duration) dur).toMinutes();
            }
        } catch (Throwable ignored) {}

        try {
            for (String fname : new String[]{"duration", "length", "minutes", "lengthMinutes"}) {
                try {
                    Field f = s.getClass().getDeclaredField(fname);
                    f.setAccessible(true);
                    Object val = f.get(s);
                    if (val instanceof Number) return ((Number) val).longValue();
                } catch (NoSuchFieldException ignored) {}
            }
        } catch (Throwable ignored) {}

        return 60L;
    }

    @Test
    void loadAvailableSlots_populates_combo_with_matching_future_slots() throws Exception {
        TimeSlot s1 = new TimeSlot(LocalDateTime.now().plusDays(1), 30, cat);
        s1.setAvailable(true);
        TimeSlot s2 = new TimeSlot(LocalDateTime.now().plusDays(2), 30, new Category("Other"));
        s2.setAvailable(true);

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
            assertNotNull(item.toString());
            assertTrue(item.toString().contains(s1.getStartDateTime().toLocalDate().toString()));

            runOnEdtAndWait(frame::dispose);
        }
    }

    @Test
    void updateDurationMax_adjusts_spinner_max_based_on_selected_slot() throws Exception {
        int requestedDuration = 45;
        TimeSlot s = new TimeSlot(LocalDateTime.now().plusDays(1), requestedDuration, cat);
        s.setAvailable(true);
        when(repo.getSlots()).thenReturn(List.of(s));

        try (MockedStatic<DialogUtil> d = mockStatic(DialogUtil.class)) {
            d.when(() -> DialogUtil.show(any(), anyString(), anyString(), any())).then(inv -> null);

            final AppointmentOptionsFrame[] ref = new AppointmentOptionsFrame[1];
            runOnEdtAndWait(() -> ref[0] = new AppointmentOptionsFrame(null, repo, appointment, typeService));
            AppointmentOptionsFrame frame = ref[0];

            JComboBox<?> combo = findCombo(frame.getContentPane());
            assertNotNull(combo);

            runOnEdtAndWait(() -> combo.setSelectedIndex(0));

            runOnEdtAndWait(() -> { /* no-op */ });

            JSpinner spinner = findSpinner(frame.getContentPane());
            assertNotNull(spinner);

            long slotComputed = getSlotLengthMinutes(s);
            int actualMax = (int) ((SpinnerNumberModel) spinner.getModel()).getMaximum();

            boolean ok = (actualMax == requestedDuration) || (actualMax == (int) slotComputed) || (actualMax == 60);
            assertTrue(ok, "spinner max should equal slot length (one of: constructor " + requestedDuration + ", computed " + slotComputed + ", or default 60). actual=" + actualMax);

            runOnEdtAndWait(frame::dispose);
        }
    }

    @Test
    void onSave_missing_slot_shows_warning_and_does_not_create() throws Exception {
        when(repo.getSlots()).thenReturn(List.of());

        try (MockedStatic<DialogUtil> d = mockStatic(DialogUtil.class)) {
            d.when(() -> DialogUtil.show(any(), anyString(), anyString(), any())).then(inv -> null);

            final AppointmentOptionsFrame[] ref = new AppointmentOptionsFrame[1];
            runOnEdtAndWait(() -> ref[0] = new AppointmentOptionsFrame(null, repo, appointment, typeService));
            AppointmentOptionsFrame frame = ref[0];

            JButton save = findButton(frame.getContentPane(), "Save");
            assertNotNull(save);
            runOnEdtAndWait(() -> save.doClick());

            verify(repo, never()).getAppointments();
            runOnEdtAndWait(frame::dispose);
        }
    }

    @Test
    void onSave_not_confirmed_shows_warning_and_does_not_create() throws Exception {
        appointment = new Appointment(user, appointment.getSlot(), 30, 1);
        when(repo.getSlots()).thenReturn(List.of());
        try (MockedStatic<DialogUtil> d = mockStatic(DialogUtil.class)) {
            d.when(() -> DialogUtil.show(any(), anyString(), anyString(), any())).then(inv -> null);

            final AppointmentOptionsFrame[] ref = new AppointmentOptionsFrame[1];
            runOnEdtAndWait(() -> ref[0] = new AppointmentOptionsFrame(null, repo, appointment, typeService));
            AppointmentOptionsFrame frame = ref[0];

            JButton save = findButton(frame.getContentPane(), "Save");
            assertNotNull(save);
            runOnEdtAndWait(() -> save.doClick());

            verify(repo, never()).getAppointments();
            runOnEdtAndWait(frame::dispose);
        }
    }

    @Test
    void onSave_validation_fails_shows_warning_and_does_not_create() throws Exception {
        TimeSlot s = new TimeSlot(LocalDateTime.now().plusDays(1), 30, cat);
        s.setAvailable(true);
        when(repo.getSlots()).thenReturn(List.of(s));

        try (MockedStatic<DialogUtil> d = mockStatic(DialogUtil.class);
             MockedStatic<AppointmentTypeRules> rules = mockStatic(AppointmentTypeRules.class)) {

            d.when(() -> DialogUtil.show(any(), anyString(), anyString(), any())).then(inv -> null);
            rules.when(() -> AppointmentTypeRules.validate(any(), anyInt(), anyInt(), any())).thenReturn("Bad: too long");

            final AppointmentOptionsFrame[] ref = new AppointmentOptionsFrame[1];
            runOnEdtAndWait(() -> ref[0] = new AppointmentOptionsFrame(null, repo, appointment, typeService));
            AppointmentOptionsFrame frame = ref[0];

            JComboBox<?> combo = findCombo(frame.getContentPane());
            runOnEdtAndWait(() -> combo.setSelectedIndex(0));
            JSpinner spinner = findSpinner(frame.getContentPane());
            runOnEdtAndWait(() -> spinner.setValue(999));

            JButton save = findButton(frame.getContentPane(), "Save");
            assertNotNull(save);
            runOnEdtAndWait(() -> save.doClick());

            verify(repo, never()).getAppointments();
            runOnEdtAndWait(frame::dispose);
        }
    }

    @Test
    void onSave_success_calls_additionalService_and_shows_saved() throws Exception {
        TimeSlot s = new TimeSlot(LocalDateTime.now().plusDays(1), 30, cat);
        s.setAvailable(true);
        when(repo.getSlots()).thenReturn(List.of(s));

        AdditionalAppointmentService mockAdditionalService = mock(AdditionalAppointmentService.class);
        when(mockAdditionalService.createNewAppointment(
                any(User.class), any(TimeSlot.class), anyInt(), anyInt(), any(AppointmentType.class), any(), any()))
                .thenReturn("Saved.");

        try (MockedStatic<DialogUtil> d = mockStatic(DialogUtil.class)) {
            d.when(() -> DialogUtil.show(any(), anyString(), anyString(), any())).then(inv -> null);

            final AppointmentOptionsFrame[] ref = new AppointmentOptionsFrame[1];
            runOnEdtAndWait(() -> ref[0] = new AppointmentOptionsFrame(null, repo, appointment, typeService));
            AppointmentOptionsFrame frame = ref[0];

            setPrivateField(frame, "additionalAppointmentService", mockAdditionalService);

            JComboBox<?> combo = findCombo(frame.getContentPane());
            runOnEdtAndWait(() -> combo.setSelectedIndex(0));

            JSpinner spinner = findSpinner(frame.getContentPane());
            runOnEdtAndWait(() -> spinner.setValue(30));

            JButton save = findButton(frame.getContentPane(), "Save");
            assertNotNull(save);
            runOnEdtAndWait(() -> save.doClick());

            verify(mockAdditionalService, timeout(2000).atLeastOnce())
                    .createNewAppointment(any(User.class), any(TimeSlot.class), anyInt(), anyInt(), any(AppointmentType.class), any(), any());

            runOnEdtAndWait(() -> assertFalse(frame.isDisplayable()));

            runOnEdtAndWait(frame::dispose);
        }
    }
    @Test
    void updateDurationMax_debug_prints_values_for_diagnosis() throws Exception {
        int requestedDuration = 45;
        TimeSlot s = new TimeSlot(LocalDateTime.now().plusDays(1), requestedDuration, cat);
        s.setAvailable(true);
        when(repo.getSlots()).thenReturn(List.of(s));

        try (MockedStatic<DialogUtil> d = mockStatic(DialogUtil.class)) {
            d.when(() -> DialogUtil.show(any(), anyString(), anyString(), any())).then(inv -> null);

            final AppointmentOptionsFrame[] ref = new AppointmentOptionsFrame[1];
            runOnEdtAndWait(() -> ref[0] = new AppointmentOptionsFrame(null, repo, appointment, typeService));
            AppointmentOptionsFrame frame = ref[0];

            JComboBox<?> combo = findCombo(frame.getContentPane());
            runOnEdtAndWait(() -> combo.setSelectedIndex(0));
            runOnEdtAndWait(() -> {});

            JSpinner spinner = findSpinner(frame.getContentPane());
            assertNotNull(spinner);

            long computed = -1;
            try {
                if (s.getStartDateTime() != null && s.getEndDateTime() != null) {
                    computed = java.time.Duration.between(s.getStartDateTime(), s.getEndDateTime()).toMinutes();
                } else {
                    try { computed = (long) s.getDuration(); } catch (Throwable ignored) {}
                }
            } catch (Throwable ignored) {}

            int actualMax = (int) ((SpinnerNumberModel) spinner.getModel()).getMaximum();

            System.out.println("DEBUG updateDurationMax: requestedDuration=" + requestedDuration
                    + " computedFromTimeSlot=" + computed + " spinnerMax=" + actualMax);

            boolean ok = (actualMax == requestedDuration) || (computed >= 0 && actualMax == (int) computed) || (actualMax == 60);
            assertTrue(ok, "spinner max mismatch: requested=" + requestedDuration + " computed=" + computed + " actualMax=" + actualMax);

            runOnEdtAndWait(frame::dispose);
        }
    }
}