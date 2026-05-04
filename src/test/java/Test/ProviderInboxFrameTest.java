package Test;

import domain.ContactRequest;
import domain.Provider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import persistence.DataRepository;
import presentation.ProviderInboxFrame;
import service.AuthService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


public class ProviderInboxFrameTest {

    private DataRepository repo;
    private AuthService auth;
    private Provider provider;

    private static void runOnEdtAndWait(Runnable r) throws Exception {
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
            return;
        }
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

    @BeforeEach
    void setUp() {
        repo = mock(DataRepository.class);
        auth = mock(AuthService.class);

        provider = mock(Provider.class);
        when(provider.getUsername()).thenReturn("prov1");

        when(auth.isLoggedIn()).thenReturn(true);
        when(auth.getCurrentUser()).thenReturn(provider);
    }

    @AfterEach
    void tearDown() {
        for (Window w : Window.getWindows()) {
            if (w != null) w.dispose();
        }
    }

    private JTable findTable(Container root) {
        for (Component c : root.getComponents()) {
            if (c instanceof JScrollPane) {
                Component v = ((JScrollPane) c).getViewport().getView();
                if (v instanceof JTable) return (JTable) v;
                if (v instanceof JPanel) {
                    JTable t = findTable((Container) v);
                    if (t != null) return t;
                }
            } else if (c instanceof JTable) return (JTable) c;
            else if (c instanceof Container) {
                JTable t = findTable((Container) c);
                if (t != null) return t;
            }
        }
        return null;
    }

    @Test
    void loadInbox_shows_requests_in_table() throws Exception {
        ContactRequest r1 = mock(ContactRequest.class);
        when(r1.getId()).thenReturn(11);
        when(r1.getFromUsername()).thenReturn("alice");
        when(r1.getCreatedAt()).thenReturn(LocalDateTime.now().plusDays(1));
        when(r1.isRead()).thenReturn(false);
        when(r1.getMessage()).thenReturn("Hello A");

        ContactRequest r2 = mock(ContactRequest.class);
        when(r2.getId()).thenReturn(22);
        when(r2.getFromUsername()).thenReturn("bob");
        when(r2.getCreatedAt()).thenReturn(LocalDateTime.now().plusDays(2));
        when(r2.isRead()).thenReturn(true);
        when(r2.getMessage()).thenReturn("Hello B");

        when(repo.getRequestsForProvider("prov1")).thenReturn(List.of(r1, r2));

        final ProviderInboxFrame[] ref = new ProviderInboxFrame[1];
        runOnEdtAndWait(() -> ref[0] = new ProviderInboxFrame(auth, repo));
        ProviderInboxFrame frame = ref[0];

        JTable table = findTable(frame.getContentPane());
        assertNotNull(table, "Table should exist in frame");
        DefaultTableModel model = (DefaultTableModel) table.getModel();

        assertEquals(2, model.getRowCount(), "Should display 2 requests");

        Object id0 = model.getValueAt(0, 0);
        Object from0 = model.getValueAt(0, 1);
        Object read0 = model.getValueAt(0, 3);
        Object msg0 = model.getValueAt(0, 4);

        assertEquals(11, Integer.parseInt(id0.toString()));
        assertEquals("alice", from0.toString());
        assertEquals("NO", read0.toString());
        assertEquals("Hello A", msg0.toString());

        Object read1 = model.getValueAt(1, 3);
        assertEquals("YES", read1.toString());

        runOnEdtAndWait(frame::dispose);
    }

    @Test
    void markSelectedRead_invokes_repo_and_updates_table() throws Exception {
        ContactRequest r1 = mock(ContactRequest.class);
        when(r1.getId()).thenReturn(101);
        when(r1.getFromUsername()).thenReturn("alice");
        when(r1.getCreatedAt()).thenReturn(LocalDateTime.now().plusDays(1));
        when(r1.isRead()).thenReturn(false);
        when(r1.getMessage()).thenReturn("Please help");

        ContactRequest r1Read = mock(ContactRequest.class);
        when(r1Read.getId()).thenReturn(101);
        when(r1Read.getFromUsername()).thenReturn("alice");
        when(r1Read.getCreatedAt()).thenReturn(LocalDateTime.now().plusDays(1));
        when(r1Read.isRead()).thenReturn(true);
        when(r1Read.getMessage()).thenReturn("Please help");

        when(repo.getRequestsForProvider("prov1")).thenReturn(List.of(r1)).thenReturn(List.of(r1Read));

        
        final ProviderInboxFrame[] ref = new ProviderInboxFrame[1];
        runOnEdtAndWait(() -> ref[0] = new ProviderInboxFrame(auth, repo));
        ProviderInboxFrame frame = ref[0];

        JTable table = findTable(frame.getContentPane());
        assertNotNull(table);
        DefaultTableModel model = (DefaultTableModel) table.getModel();

        assertEquals(1, model.getRowCount());
        assertEquals("NO", model.getValueAt(0, 3));

        runOnEdtAndWait(() -> {
            table.setRowSelectionInterval(0, 0);
            try {
                Method m = ProviderInboxFrame.class.getDeclaredMethod("markSelectedRead");
                m.setAccessible(true);
                m.invoke(frame);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });

        verify(repo, atLeastOnce()).markRequestRead(101);

        runOnEdtAndWait(() -> {
            DefaultTableModel m2 = (DefaultTableModel) table.getModel();
            assertEquals(1, m2.getRowCount());
            assertEquals("YES", m2.getValueAt(0, 3));
        });

        runOnEdtAndWait(frame::dispose);
    }
}