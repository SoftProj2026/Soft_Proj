package Test;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Transport;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import service.EmailService;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EmailServiceTest {

    private static void invokeRun() throws Exception {
        Method run = EmailService.class.getDeclaredMethod("run");
        run.setAccessible(true);

        try {
            run.invoke(null);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();

            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }

            if (cause instanceof Error error) {
                throw error;
            }

            throw e;
        }
    }

    private static boolean invokeIsBlank(String value) throws Exception {
        Method isBlank = EmailService.class.getDeclaredMethod("isBlank", String.class);
        isBlank.setAccessible(true);
        return (boolean) isBlank.invoke(null, value);
    }

    @Test
    void constructor_createsService() {
        EmailService service = new EmailService("test@gmail.com", "secret");

        assertNotNull(service);
    }

    @Test
    void sendEmail_success_buildsMessageAndCallsTransportSend() {
        EmailService service = new EmailService("sender@gmail.com", "secret");

        try (MockedStatic<Transport> transport = mockStatic(Transport.class)) {
            transport.when(() -> Transport.send(any(Message.class)))
                    .thenAnswer(inv -> null);

            assertDoesNotThrow(() ->
                    service.sendEmail(
                            "receiver@gmail.com",
                            "Test Subject",
                            "Test Body"
                    )
            );

            transport.verify(() -> Transport.send(any(Message.class)), times(1));
        }
    }

    @Test
    void sendEmail_invalidRecipient_wrapsMessagingExceptionInRuntimeException() {
        EmailService service = new EmailService("sender@gmail.com", "secret");

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                service.sendEmail(
                        "",
                        "Test Subject",
                        "Test Body"
                )
        );

        assertEquals("Failed to send email", ex.getMessage());
        assertTrue(ex.getCause() instanceof MessagingException);
    }

    @Test
    void sendEmail_whenTransportThrows_wrapsException() {
        EmailService service = new EmailService("sender@gmail.com", "secret");

        try (MockedStatic<Transport> transport = mockStatic(Transport.class)) {
            transport.when(() -> Transport.send(any(Message.class)))
                    .thenThrow(new MessagingException("SMTP failed"));

            RuntimeException ex = assertThrows(RuntimeException.class, () ->
                    service.sendEmail(
                            "receiver@gmail.com",
                            "Subject",
                            "Body"
                    )
            );

            assertEquals("Failed to send email", ex.getMessage());
            assertTrue(ex.getCause() instanceof MessagingException);

            transport.verify(() -> Transport.send(any(Message.class)), times(1));
        }
    }

    @Test
    void run_missingUsername_throwsIllegalStateException() {
        Dotenv dotenv = mock(Dotenv.class);

        when(dotenv.get("EMAIL_USERNAME")).thenReturn(null);
        when(dotenv.get("EMAIL_SECRET")).thenReturn("secret");

        try (MockedStatic<Dotenv> dotenvStatic = mockStatic(Dotenv.class)) {
            dotenvStatic.when(Dotenv::load).thenReturn(dotenv);

            IllegalStateException ex = assertThrows(
                    IllegalStateException.class,
                    EmailServiceTest::invokeRun
            );

            assertTrue(ex.getMessage().contains("Missing email credentials"));
            assertTrue(ex.getMessage().contains("EMAIL_USERNAME"));
            assertTrue(ex.getMessage().contains("EMAIL_SECRET"));
        }
    }

    @Test
    void run_missingSecret_throwsIllegalStateException() {
        Dotenv dotenv = mock(Dotenv.class);

        when(dotenv.get("EMAIL_USERNAME")).thenReturn("sender@gmail.com");
        when(dotenv.get("EMAIL_SECRET")).thenReturn("   ");

        try (MockedStatic<Dotenv> dotenvStatic = mockStatic(Dotenv.class)) {
            dotenvStatic.when(Dotenv::load).thenReturn(dotenv);

            IllegalStateException ex = assertThrows(
                    IllegalStateException.class,
                    EmailServiceTest::invokeRun
            );

            assertTrue(ex.getMessage().contains("Missing email credentials"));
        }
    }

    @Test
    void run_withCredentials_sendsTwoDemoEmails() throws Exception {
        Dotenv dotenv = mock(Dotenv.class);

        when(dotenv.get("EMAIL_USERNAME")).thenReturn(" sender@gmail.com ");
        when(dotenv.get("EMAIL_SECRET")).thenReturn(" app-secret ");

        try (MockedStatic<Dotenv> dotenvStatic = mockStatic(Dotenv.class);
             MockedConstruction<EmailService> constructed =
                     mockConstruction(EmailService.class)) {

            dotenvStatic.when(Dotenv::load).thenReturn(dotenv);

            invokeRun();

            assertEquals(1, constructed.constructed().size());

            EmailService mockService = constructed.constructed().get(0);

            verify(mockService, times(1)).sendEmail(
                    "remaajomaa70@gmail.com",
                    "Book Due Reminder",
                    "Dear user, your appointment is coming soon. Best regards."
            );

            verify(mockService, times(1)).sendEmail(
                    "remaajomaa842@gmail.com",
                    "Book Due Reminder",
                    "Dear user, your appointment is coming soon. Best regards."
            );
        }
    }

    @Test
    void main_callsRun() {
        Dotenv dotenv = mock(Dotenv.class);

        when(dotenv.get("EMAIL_USERNAME")).thenReturn("sender@gmail.com");
        when(dotenv.get("EMAIL_SECRET")).thenReturn("secret");

        try (MockedStatic<Dotenv> dotenvStatic = mockStatic(Dotenv.class);
             MockedConstruction<EmailService> constructed =
                     mockConstruction(EmailService.class)) {

            dotenvStatic.when(Dotenv::load).thenReturn(dotenv);

            assertDoesNotThrow(() -> EmailService.main(new String[0]));

            assertEquals(1, constructed.constructed().size());
        }
    }

    @Test
    void isBlank_handlesNullEmptySpacesAndValue() throws Exception {
        assertTrue(invokeIsBlank(null));
        assertTrue(invokeIsBlank(""));
        assertTrue(invokeIsBlank("   "));
        assertFalse(invokeIsBlank("abc"));
        assertFalse(invokeIsBlank(" abc "));
    }
}