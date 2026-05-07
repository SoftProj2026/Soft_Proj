package Test;

import org.junit.jupiter.api.Test;
import service.EmailService;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class EmailServiceTest {

    @Test
    void testCreateEmailService() {
        assertDoesNotThrow(() -> {
            EmailService service =
                    new EmailService("test@gmail.com", "secret");

            assert service != null;
        });
    }

    @Test
    void testSendEmailWithInvalidCredentials() {
        EmailService service =
                new EmailService("fake@gmail.com", "wrongpassword");

        assertThrows(RuntimeException.class, () -> {
            service.sendEmail(
                    "receiver@gmail.com",
                    "Test Subject",
                    "Test Body"
            );
        });
    }

    @Test
    void testSendEmailWithEmptyRecipient() {
        EmailService service =
                new EmailService("fake@gmail.com", "wrongpassword");

        assertThrows(RuntimeException.class, () -> {
            service.sendEmail(
                    "",
                    "Test Subject",
                    "Test Body"
            );
        });
    }

    @Test
    void testSendEmailWithNullRecipient() {
        EmailService service =
                new EmailService("fake@gmail.com", "wrongpassword");

        assertThrows(RuntimeException.class, () -> {
            service.sendEmail(
                    null,
                    "Test Subject",
                    "Test Body"
            );
        });
    }
}