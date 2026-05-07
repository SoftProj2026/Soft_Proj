package Test;

import domain.*;
import org.junit.jupiter.api.Test;
import persistence.DataRepository;
import presentation.AdminRequestsFrame;
import presentation.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Assumptions;
class AdminRequestsFrameTest {
    @BeforeAll
    static void skipIfHeadless() {
        Assumptions.assumeFalse(
            java.awt.GraphicsEnvironment.isHeadless(),
            "Skipping GUI tests in CI headless mode"
        );
    }
    private static void runOnEdt(Runnable r) throws Exception {
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            final Throwable[] err = new Throwable[1];
            SwingUtilities.invokeAndWait(() -> {
                try {
                    r.run();
                } catch (Throwable t) {
                    err[0] = t;
                }
            });
            if (err[0] != null) throw new RuntimeException(err[0]);
        }
    }

    @SuppressWarnings("unchecked")
    private JTable findTable(Container root) {
        for (Component c : root.getComponents()) {
            if (c instanceof JScrollPane) {
                JViewport vp = ((JScrollPane) c).getViewport();
                Component view = vp.getView();
                if (view instanceof JTable) return (JTable) view;
            }
            if (c instanceof Container) {
                JTable t = findTable((Container) c);
                if (t != null) return t;
            }
        }
        return null;
    }

    private JLabel findSubtitleLabel(Container root) {
        for (Component c : root.getComponents()) {
            if (c instanceof JLabel) {
                String txt = ((JLabel) c).getText();
                if (txt != null && (txt.contains("Category Admin view") || txt.contains("QR Admin for Company view"))) {
                    return (JLabel) c;
                }
            }
            if (c instanceof Container) {
                JLabel l = findSubtitleLabel((Container) c);
                if (l != null) return l;
            }
        }
        return null;
    }

    @Test
    void categoryAdmin_loads_only_assigned_requests_into_table() throws Exception {
        runOnEdt(() -> UITheme.apply());

        DataRepository repo = mock(DataRepository.class);

        BookingRequest req = mock(BookingRequest.class);
        User requester = new User("First", "Last", "user1", "pw", java.time.LocalDate.of(1990,1,1), "a@b.com");
        Category cat = new Category("CatX");
        TimeSlot slot = new TimeSlot(LocalDateTime.now(), 60, cat);

        when(req.getId()).thenReturn(100); 
        when(req.getRequester()).thenReturn(requester);
        when(req.getSlot()).thenReturn(slot);
        when(req.getDurationInMinutes()).thenReturn(60);
        when(req.getParticipants()).thenReturn(1);
        when(req.getStatus()).thenReturn(BookingRequestStatus.PENDING_CATEGORY_ADMIN);
        when(req.getCategoryAdminUsername()).thenReturn("catadmin");

        doReturn(List.of(req)).when(repo).getRequestsForCategoryAdmin("catadmin");

        Administrator adminUser = new Administrator("catadmin", "pw");

        final AdminRequestsFrame[] fref = new AdminRequestsFrame[1];
        runOnEdt(() -> fref[0] = new AdminRequestsFrame(repo, adminUser));
        AdminRequestsFrame frame = fref[0];

        final DefaultTableModel[] modelRef = new DefaultTableModel[1];
        runOnEdt(() -> {
            JTable table = findTable(frame.getContentPane());
            assertNotNull(table, "Table must be present");
            modelRef[0] = (DefaultTableModel) table.getModel();
        });

        DefaultTableModel model = modelRef[0];
        assertEquals(1, model.getRowCount());
        assertEquals(100, model.getValueAt(0, 0));
        assertEquals("user1", model.getValueAt(0, 1));
        assertEquals("CatX", model.getValueAt(0, 2));
        assertEquals(60, model.getValueAt(0, 4));
        assertEquals(1, model.getValueAt(0, 5));
        assertEquals("PENDING_CATEGORY_ADMIN", model.getValueAt(0, 6));
        assertEquals("catadmin", model.getValueAt(0, 7));

        runOnEdt(() -> frame.dispose());
    }

    @Test
    void bigAdmin_loads_big_admin_requests_and_shows_role_hint() throws Exception {
        runOnEdt(() -> UITheme.apply());

        DataRepository repo = mock(DataRepository.class);

        BookingRequest req = mock(BookingRequest.class);
        User requester = new User("A", "B", "u2", "pw", java.time.LocalDate.of(1992,2,2), "b@c.com");
        Category cat = new Category("CatY");
        TimeSlot slot = new TimeSlot(LocalDateTime.now(), 30, cat);

        when(req.getId()).thenReturn(200); 
        when(req.getRequester()).thenReturn(requester);
        when(req.getSlot()).thenReturn(slot);
        when(req.getDurationInMinutes()).thenReturn(30);
        when(req.getParticipants()).thenReturn(2);
        when(req.getStatus()).thenReturn(BookingRequestStatus.PENDING_BIG_ADMIN);
        when(req.getCategoryAdminUsername()).thenReturn("catadmin2");

        doReturn(List.of(req)).when(repo).getRequestsForBigAdmin();

        Administrator adminUser = new Administrator("admin", "pw");

        final AdminRequestsFrame[] fref = new AdminRequestsFrame[1];
        runOnEdt(() -> fref[0] = new AdminRequestsFrame(repo, adminUser));
        AdminRequestsFrame frame = fref[0];

        final DefaultTableModel[] modelRef = new DefaultTableModel[1];
        final JLabel[] subtitleRef = new JLabel[1];

        runOnEdt(() -> {
            JTable table = findTable(frame.getContentPane());
            assertNotNull(table, "Table must be present");
            modelRef[0] = (DefaultTableModel) table.getModel();
            subtitleRef[0] = findSubtitleLabel(frame.getContentPane());
        });

        DefaultTableModel model = modelRef[0];
        assertEquals(1, model.getRowCount());
        assertEquals(200, model.getValueAt(0, 0));
        assertEquals("u2", model.getValueAt(0, 1));
        assertEquals("CatY", model.getValueAt(0, 2));
        assertEquals(30, model.getValueAt(0, 4));
        assertEquals(2, model.getValueAt(0, 5));
        assertEquals("PENDING_BIG_ADMIN", model.getValueAt(0, 6));
        assertEquals("catadmin2", model.getValueAt(0, 7));

        JLabel subtitle = subtitleRef[0];
        assertNotNull(subtitle);
        assertTrue(subtitle.getText().toLowerCase().contains("qr admin for company") || subtitle.getText().toLowerCase().contains("final approve"));

        runOnEdt(() -> frame.dispose());
    }
}