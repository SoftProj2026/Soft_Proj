package Test;

import domain.*;
import persistence.DataRepository;
import presentation.AdminActivityFrame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Assumptions;
/**
 * Unit tests for AdminActivityFrame without using reflection on private members.
 * All Swing interaction runs on the EDT using SwingUtilities.invokeAndWait.
 */
class AdminActivityFrameTest {
    @BeforeAll
    static void skipIfHeadless() {
        Assumptions.assumeFalse(
            java.awt.GraphicsEnvironment.isHeadless(),
            "Skipping GUI tests in CI headless mode"
        );
    }
    private DataRepository repo;
    private AdminActivityFrame frame;

    @BeforeEach
    void setUp() throws Exception {
        repo = new DataRepository();

        ContactRequest msg = new ContactRequest("ali", "provX", "hello admin");
        repo.getContactRequests().add(msg);

        Category cat = new Category("CatA");
        repo.addCategory(cat);
        User user = new User("F", "L", "u1", "pw", LocalDate.of(2000, 1, 1), "mail@test.com");
        repo.addUser(user);
        TimeSlot slot = new TimeSlot(LocalDateTime.of(2023, 4, 1, 12, 0), 30, cat);
        repo.addSlot(slot);

        Appointment ap = new Appointment(user, slot, 30, 1);
        repo.addAppointment(ap);

        AuditEvent ev = new AuditEvent(AuditEvent.Type.MESSAGE_SENT, "admin", "system", "msg sent");
        repo.getAuditEvents().add(ev);

        SwingUtilities.invokeAndWait(() -> frame = new AdminActivityFrame(repo));
    }

    private JTabbedPane getTabbedPane() throws Exception {
        final JTabbedPane[] result = new JTabbedPane[1];
        SwingUtilities.invokeAndWait(() -> {
            for (Component c : frame.getContentPane().getComponents()) {
                if (c instanceof JTabbedPane) {
                    result[0] = (JTabbedPane) c;
                    return;
                }
            }
            result[0] = null;
        });
        return result[0];
    }

    private JTable getTableFromPanel(final JPanel panel) {
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JScrollPane) {
                JViewport vp = ((JScrollPane) comp).getViewport();
                Component view = vp.getView();
                if (view instanceof JTable) return (JTable) view;
            }
        }
        for (Component comp : panel.getComponents()) {
            if (comp instanceof Container) {
                JTable t = findTableRecursively((Container) comp);
                if (t != null) return t;
            }
        }
        return null;
    }

    private JTable findTableRecursively(Container c) {
        for (Component comp : c.getComponents()) {
            if (comp instanceof JTable) return (JTable) comp;
            if (comp instanceof Container) {
                JTable t = findTableRecursively((Container) comp);
                if (t != null) return t;
            }
        }
        return null;
    }

    @Test
    void messagesPanel_displays_expected_requests() throws Exception {
        JTabbedPane tabs = getTabbedPane();
        assertNotNull(tabs, "Tabbed pane should exist in frame");

        Component comp = tabs.getComponentAt(0);
        assertTrue(comp instanceof JPanel, "Messages tab should be a JPanel");
        JPanel panel = (JPanel) comp;

        final DefaultTableModel[] modelRef = new DefaultTableModel[1];
        SwingUtilities.invokeAndWait(() -> {
            JTable table = getTableFromPanel(panel);
            assertNotNull(table, "Messages table must be present");
            modelRef[0] = (DefaultTableModel) table.getModel();
        });

        DefaultTableModel model = modelRef[0];
        assertEquals(1, model.getRowCount());
        assertEquals("ali", model.getValueAt(0, 1));
        assertEquals("provX", model.getValueAt(0, 2));
        assertEquals("hello admin", model.getValueAt(0, 4));
    }

    @Test
    void appointmentsPanel_displays_expected_appointments() throws Exception {
        JTabbedPane tabs = getTabbedPane();
        assertNotNull(tabs);

        Component comp = tabs.getComponentAt(1);
        assertTrue(comp instanceof JPanel);
        JPanel panel = (JPanel) comp;

        final DefaultTableModel[] modelRef = new DefaultTableModel[1];
        SwingUtilities.invokeAndWait(() -> {
            JTable table = getTableFromPanel(panel);
            assertNotNull(table, "Appointments table must be present");
            modelRef[0] = (DefaultTableModel) table.getModel();
        });

        DefaultTableModel model = modelRef[0];
        assertEquals(1, model.getRowCount());
        assertEquals("u1", model.getValueAt(0, 1));
        assertEquals("CatA", model.getValueAt(0, 2));
        assertNotNull(model.getValueAt(0, 3));
        assertEquals("PENDING", model.getValueAt(0, 7)); 
    }

    @Test
    void auditPanel_displays_expected_events() throws Exception {
        JTabbedPane tabs = getTabbedPane();
        assertNotNull(tabs);

        Component comp = tabs.getComponentAt(2);
        assertTrue(comp instanceof JPanel);
        JPanel panel = (JPanel) comp;

        final DefaultTableModel[] modelRef = new DefaultTableModel[1];
        SwingUtilities.invokeAndWait(() -> {
            JTable table = getTableFromPanel(panel);
            assertNotNull(table, "Audit table must be present");
            modelRef[0] = (DefaultTableModel) table.getModel();
        });

        DefaultTableModel model = modelRef[0];
        assertEquals(1, model.getRowCount());
        assertEquals("admin", model.getValueAt(0, 2));
        assertEquals("system", model.getValueAt(0, 3));
        assertEquals("msg sent", model.getValueAt(0, 5));
    }

    @Test
    void tabs_exist_and_have_expected_names() throws Exception {
        JTabbedPane tabs = getTabbedPane();
        assertNotNull(tabs);

        String t0 = tabs.getTitleAt(0);
        String t1 = tabs.getTitleAt(1);
        String t2 = tabs.getTitleAt(2);

        assertTrue(t0.equals("Messages") || t1.equals("Messages") || t2.equals("Messages"));
        assertTrue(t0.equals("Appointments") || t1.equals("Appointments") || t2.equals("Appointments"));
        assertTrue(t0.equals("Audit Log") || t1.equals("Audit Log") || t2.equals("Audit Log"));
    }
}