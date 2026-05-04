package Test;

import domain.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.stubbing.Answer;
import persistence.DataRepository;
import presentation.AdminManageReservationsFrame;
import presentation.DialogUtil;
import presentation.UITheme;
import service.AuthService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Robust headless tests for AdminManageReservationsFrame.
 * Requires mockito-inline (or Mockito with mockStatic support) on the test classpath.
 */
class AdminManageReservationsFrameTest {

    private static void runOnEdt(Runnable r) throws Exception {
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            final Throwable[] err = new Throwable[1];
            SwingUtilities.invokeAndWait(() -> {
                try { r.run(); } catch (Throwable t) { err[0] = t; }
            });
            if (err[0] != null) throw new RuntimeException(err[0]);
        }
    }

    DataRepository repo;
    AuthService auth;
    List<Appointment> appointments;
    List<TimeSlot> slots;

    MockedStatic<JOptionPane> jops;
    MockedStatic<DialogUtil> dutil;

    @BeforeEach
    void setUp() {
        UITheme.apply();

        appointments = new ArrayList<>();
        slots = new ArrayList<>();

        repo = mock(DataRepository.class);
        when(repo.getAppointments()).thenAnswer(inv -> appointments);
        when(repo.getSlots()).thenAnswer(inv -> slots);

        auth = mock(AuthService.class);
        User adminUser = new User("A","B","admin","pw", java.time.LocalDate.of(1990,1,1), "a@b.com");
        when(auth.isLoggedIn()).thenReturn(true);
        when(auth.getCurrentUser()).thenReturn(adminUser);

        jops = mockStatic(JOptionPane.class);
        dutil = mockStatic(DialogUtil.class);
        dutil.when(() -> DialogUtil.show(any(), anyString(), anyString(), any())).then(inv -> null);
    }

    @AfterEach
    void tearDown() {
        if (jops != null) jops.close();
        if (dutil != null) dutil.close();
    }

    private JTable findTable(Container root) {
        for (Component c : root.getComponents()) {
            if (c instanceof JScrollPane) {
                Component v = ((JScrollPane) c).getViewport().getView();
                if (v instanceof JTable) return (JTable) v;
            }
            if (c instanceof Container) {
                JTable t = findTable((Container) c);
                if (t != null) return t;
            }
        }
        return null;
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
    void load_displays_all_appointments_and_columns() throws Exception {
        Category cat = new Category("C");
        TimeSlot future = new TimeSlot(LocalDateTime.now().plusDays(2), 30, cat);
        TimeSlot past = new TimeSlot(LocalDateTime.now().minusDays(2), 30, cat);
        slots.add(future);
        slots.add(past);

        User u = new User("F","L","u1","pw", java.time.LocalDate.of(1995,1,1), "a@b.com");

        Appointment a1 = new Appointment(u, future, 30, 2);
        a1.confirm();
        Appointment a2 = new Appointment(u, past, 30, 1);
        a2.confirm(); a2.cancel();

        appointments.add(a1);
        appointments.add(a2);

        final AdminManageReservationsFrame[] ref = new AdminManageReservationsFrame[1];
        runOnEdt(() -> ref[0] = new AdminManageReservationsFrame(auth, repo));
        AdminManageReservationsFrame frame = ref[0];

        runOnEdt(() -> {
            JTable table = findTable(frame.getContentPane());
            assertNotNull(table, "table present");
            DefaultTableModel model = (DefaultTableModel) table.getModel();

            assertEquals(2, model.getRowCount(), "two appointments shown");
            assertEquals(8, model.getColumnCount(), "eight columns defined");

            String s0 = (String) model.getValueAt(0, 7);
            String s1 = (String) model.getValueAt(1, 7);
            assertTrue(s0.equals("CONFIRMED") || s1.equals("CONFIRMED"));
            assertTrue(s0.equals("CANCELLED") || s1.equals("CANCELLED"));
        });

        runOnEdt(ref[0]::dispose);
    }

    @Test
    void cancelSelected_branches_and_successful_cancel() throws Exception {
        Category cat = new Category("C2");
        TimeSlot future = new TimeSlot(LocalDateTime.now().plusDays(2), 30, cat);
        slots.add(future);

        User u = new User("F","L","u2","pw", java.time.LocalDate.of(1995,1,1), "b@b.com");

        Appointment pending = new Appointment(u, future, 30, 1);
        appointments.add(pending);

        final AdminManageReservationsFrame[] ref = new AdminManageReservationsFrame[1];
        runOnEdt(() -> ref[0] = new AdminManageReservationsFrame(auth, repo));
        AdminManageReservationsFrame frame = ref[0];

        runOnEdt(() -> {
            JTable table = findTable(frame.getContentPane());
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            assertEquals(1, model.getRowCount());
            table.setRowSelectionInterval(0,0);

            JButton cancelBtn = findButton(frame.getContentPane(), "Cancel");
            assertNotNull(cancelBtn);
            cancelBtn.doClick();
        });


        appointments.clear();
        Appointment confirmed = new Appointment(u, future, 30, 2);
        confirmed.confirm();
        appointments.add(confirmed);

        doAnswer((Answer<String>) inv -> {
            Appointment a = inv.getArgument(0);
            a.cancel();
            return "Cancelled by admin";
        }).when(repo).adminCancelAppointment(any(Appointment.class), anyString());

        jops.when(() -> JOptionPane.showConfirmDialog(any(Component.class), any(), anyString(), anyInt()))
                .thenReturn(JOptionPane.YES_OPTION);

        runOnEdt(() -> {
            JButton refresh = findButton(frame.getContentPane(), "Refresh");
            assertNotNull(refresh);
            refresh.doClick();

            JTable table = findTable(frame.getContentPane());
            table.setRowSelectionInterval(0,0);

            JButton cancelBtn = findButton(frame.getContentPane(), "Cancel");
            cancelBtn.doClick();
        });

        verify(repo, atLeastOnce()).adminCancelAppointment(any(Appointment.class), anyString());

        runOnEdt(() -> {
            JTable table = findTable(frame.getContentPane());
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            String status = (String) model.getValueAt(0, 7);
            assertEquals("CANCELLED", status);
        });

        runOnEdt(ref[0]::dispose);
    }

    @Test
    void modifySelected_branches_and_successful_modify_flexible_verification() throws Exception {
        Category cat = new Category("CatM");
        TimeSlot slotA = new TimeSlot(LocalDateTime.now().plusDays(3), 60, cat);
        TimeSlot slotB = new TimeSlot(LocalDateTime.now().plusDays(4), 45, cat);
        slots.add(slotA);
        slots.add(slotB);

        User u = new User("X","Y","ux","pw", java.time.LocalDate.of(1992,2,2), "x@b.com");
        Appointment ap = new Appointment(u, slotA, 60, 2);
        ap.confirm();
        appointments.add(ap);

        final java.util.concurrent.atomic.AtomicReference<TimeSlot> capturedSlot = new java.util.concurrent.atomic.AtomicReference<>(null);

        doAnswer((Answer<String>) inv -> {
            Appointment old = inv.getArgument(0);
            TimeSlot newSlot = inv.getArgument(1);
            Integer duration = inv.getArgument(2);
            Integer participants = inv.getArgument(3);

            if (newSlot != null) capturedSlot.set(newSlot);

            appointments.remove(old);
            Appointment newAp = new Appointment(old.getUser(), newSlot, duration, participants);
            newAp.confirm();
            appointments.add(newAp);
            return "Modified";
        }).when(repo).modifyAppointment(any(Appointment.class), any(TimeSlot.class), anyInt(), anyInt(), anyString());

        String labelForSlotB = slotB.getStartDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        jops.when(() -> JOptionPane.showInputDialog(
                any(Component.class), any(), anyString(), anyInt(), any(), any(Object[].class), any()
        )).thenReturn(labelForSlotB);

        jops.when(() -> JOptionPane.showInputDialog(
                any(Component.class), anyString(), anyString(), anyInt()
        )).thenReturn("3", "30"); 

        jops.when(() -> JOptionPane.showConfirmDialog(
                any(Component.class), any(), anyString(), anyInt()
        )).thenReturn(JOptionPane.YES_OPTION);

        final AdminManageReservationsFrame[] ref = new AdminManageReservationsFrame[1];
        runOnEdt(() -> ref[0] = new AdminManageReservationsFrame(auth, repo));
        AdminManageReservationsFrame frame = ref[0];

        runOnEdt(() -> {
            JTable table = findTable(frame.getContentPane());
            table.setRowSelectionInterval(0,0);
            JButton modify = findButton(frame.getContentPane(), "Modify");
            assertNotNull(modify);
            modify.doClick();
        });

        verify(repo, atLeastOnce()).modifyAppointment(any(Appointment.class), any(TimeSlot.class), anyInt(), anyInt(), anyString());

        TimeSlot used = capturedSlot.get();
        assertNotNull(used, "Expected modifyAppointment to receive a new TimeSlot argument");
        assertEquals(slotB.getStartDateTime(), used.getStartDateTime(), "Expected the new slot to be slotB");

        runOnEdt(() -> {
            JTable table = findTable(frame.getContentPane());
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            boolean found = false;
            String expectedDate = slotB.getStartDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            for (int r = 0; r < model.getRowCount(); r++) {
                Object dateCell = model.getValueAt(r, 1); 
                if (dateCell != null && expectedDate.equals(dateCell.toString())) {
                    found = true;
                    break;
                }
                for (int c = 0; c < model.getColumnCount(); c++) {
                    Object cell = model.getValueAt(r, c);
                    if (cell != null && expectedDate.equals(cell.toString())) { found = true; break; }
                }
                if (found) break;
            }
            assertTrue(found, "Expected to find modified appointment in table showing slotB's date");
        });

        runOnEdt(ref[0]::dispose);
    }}