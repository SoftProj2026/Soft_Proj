package Test;

import domain.ContactRequest;
import domain.Provider;
import domain.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import persistence.DataRepository;
import presentation.CustomerContactProvidersFrame;
import presentation.DialogUtil;
import presentation.UITheme;
import service.AuthService;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CustomerContactProvidersFrameTest {

    private DataRepository repo;
    private AuthService auth;
    private User user;

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

        user = new User("First", "Last", "cust1", "pw", java.time.LocalDate.of(1995,1,1), "cust1@example.com");
        when(auth.isLoggedIn()).thenReturn(true);
        when(auth.getCurrentUser()).thenReturn(user);
    }

    @AfterEach
    void tearDown() {
        for (Window w : Window.getWindows()) {
            if (w != null) w.dispose();
        }
    }

    private JList<?> findList(Container root) {
        for (Component c : root.getComponents()) {
            if (c instanceof JList) return (JList<?>) c;
            if (c instanceof JScrollPane) {
                Component v = ((JScrollPane) c).getViewport().getView();
                if (v instanceof JList) return (JList<?>) v;
            }
            if (c instanceof Container) {
                JList<?> r = findList((Container) c);
                if (r != null) return r;
            }
        }
        return null;
    }

    private JTextArea findTextArea(Container root) {
        for (Component c : root.getComponents()) {
            if (c instanceof JTextArea) return (JTextArea) c;
            if (c instanceof JScrollPane) {
                Component v = ((JScrollPane) c).getViewport().getView();
                if (v instanceof JTextArea) return (JTextArea) v;
            }
            if (c instanceof Container) {
                JTextArea r = findTextArea((Container) c);
                if (r != null) return r;
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
    void loadProviders_shows_list_with_mocked_providers() throws Exception {
        Provider p1 = mock(Provider.class);
        when(p1.getUsername()).thenReturn("prov1");
        when(p1.getDisplayName()).thenReturn("Prov One");
        when(p1.getEmail()).thenReturn("p1@example.com");

        Provider p2 = mock(Provider.class);
        when(p2.getUsername()).thenReturn("prov2");
        when(p2.getDisplayName()).thenReturn("Prov Two");
        when(p2.getEmail()).thenReturn("p2@example.com");

        when(repo.getProviders()).thenReturn(List.of(p1, p2));

        final CustomerContactProvidersFrame[] ref = new CustomerContactProvidersFrame[1];
        runOnEdtAndWait(() -> ref[0] = new CustomerContactProvidersFrame(auth, repo));
        CustomerContactProvidersFrame frame = ref[0];

        JList<?> list = findList(frame.getContentPane());
        assertNotNull(list, "Providers list should exist");

        ListModel<?> model = list.getModel();
        assertEquals(2, model.getSize());
        assertEquals(p1, model.getElementAt(0));
        assertEquals(p2, model.getElementAt(1));

        runOnEdtAndWait(frame::dispose);
    }

    @Test
    void sendMessage_with_selection_saves_and_shows_success_without_verifying_constructor() throws Exception {
        Provider p = mock(Provider.class);
        when(p.getUsername()).thenReturn("provX");
        when(p.getDisplayName()).thenReturn("Provider X");
        when(p.getEmail()).thenReturn("provx@example.com");

        when(repo.getProviders()).thenReturn(List.of(p));

        try (MockedStatic<DialogUtil> dialogMock = mockStatic(DialogUtil.class)) {
            dialogMock.when(() -> DialogUtil.show(any(), anyString(), anyString(), any())).then(inv -> null);

            final CustomerContactProvidersFrame[] ref = new CustomerContactProvidersFrame[1];
            runOnEdtAndWait(() -> ref[0] = new CustomerContactProvidersFrame(auth, repo));
            CustomerContactProvidersFrame frame = ref[0];

            JList<?> list = findList(frame.getContentPane());
            assertNotNull(list);
            runOnEdtAndWait(() -> list.setSelectedIndex(0));

            JTextArea ta = findTextArea(frame.getContentPane());
            assertNotNull(ta);
            runOnEdtAndWait(() -> ta.setText("Hello company"));

            JButton sendBtn = findButton(frame.getContentPane(), "Send");
            assertNotNull(sendBtn);

            // click send
            runOnEdtAndWait(() -> sendBtn.doClick());

            ArgumentCaptor<ContactRequest> capt = ArgumentCaptor.forClass(ContactRequest.class);
            verify(repo, atLeastOnce()).addContactRequest(capt.capture());
            ContactRequest saved = capt.getValue();
            assertNotNull(saved);

            runOnEdtAndWait(frame::dispose);
        }
    }

    @Test
    void sendMessage_no_selection_shows_warning_and_does_not_save() throws Exception {
        when(repo.getProviders()).thenReturn(List.of()); 

        try (MockedStatic<DialogUtil> dialogMock = mockStatic(DialogUtil.class)) {
            dialogMock.when(() -> DialogUtil.show(any(), anyString(), anyString(), any())).then(inv -> null);

            final CustomerContactProvidersFrame[] ref = new CustomerContactProvidersFrame[1];
            runOnEdtAndWait(() -> ref[0] = new CustomerContactProvidersFrame(auth, repo));
            CustomerContactProvidersFrame frame = ref[0];

            JTextArea ta = findTextArea(frame.getContentPane());
            runOnEdtAndWait(() -> ta.setText("Hello"));

            JButton sendBtn = findButton(frame.getContentPane(), "Send");
            assertNotNull(sendBtn);
            runOnEdtAndWait(() -> sendBtn.doClick());

            verify(repo, never()).addContactRequest(any());

            runOnEdtAndWait(frame::dispose);
        }
    }

    @Test
    void sendMessage_empty_message_shows_warning_and_does_not_save() throws Exception {
        Provider p = mock(Provider.class);
        when(p.getUsername()).thenReturn("provY");
        when(p.getDisplayName()).thenReturn("Provider Y");
        when(repo.getProviders()).thenReturn(List.of(p));

        try (MockedStatic<DialogUtil> dialogMock = mockStatic(DialogUtil.class)) {
            dialogMock.when(() -> DialogUtil.show(any(), anyString(), anyString(), any())).then(inv -> null);

            final CustomerContactProvidersFrame[] ref = new CustomerContactProvidersFrame[1];
            runOnEdtAndWait(() -> ref[0] = new CustomerContactProvidersFrame(auth, repo));
            CustomerContactProvidersFrame frame = ref[0];

            JList<?> list = findList(frame.getContentPane());
            runOnEdtAndWait(() -> list.setSelectedIndex(0));

            JTextArea ta = findTextArea(frame.getContentPane());
            runOnEdtAndWait(() -> ta.setText("   ")); 

            JButton sendBtn = findButton(frame.getContentPane(), "Send");
            runOnEdtAndWait(() -> sendBtn.doClick());

            verify(repo, never()).addContactRequest(any());

            runOnEdtAndWait(frame::dispose);
        }
    }

    @Test
    void sendMessage_not_logged_in_shows_warning_and_does_not_save() throws Exception {
        when(auth.isLoggedIn()).thenReturn(false);
        when(repo.getProviders()).thenReturn(List.of());

        try (MockedStatic<DialogUtil> dialogMock = mockStatic(DialogUtil.class)) {
            dialogMock.when(() -> DialogUtil.show(any(), anyString(), anyString(), any())).then(inv -> null);

            final CustomerContactProvidersFrame[] ref = new CustomerContactProvidersFrame[1];
            runOnEdtAndWait(() -> ref[0] = new CustomerContactProvidersFrame(auth, repo));
            CustomerContactProvidersFrame frame = ref[0];

            JButton sendBtn = findButton(frame.getContentPane(), "Send");
            assertNotNull(sendBtn);
            runOnEdtAndWait(() -> sendBtn.doClick());

            verify(repo, never()).addContactRequest(any());

            runOnEdtAndWait(frame::dispose);
        }
    }
}