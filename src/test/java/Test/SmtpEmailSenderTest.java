package Test;

import Service.SmtpEmailSender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SmtpEmailSenderTest {

    @BeforeEach
    void before() {
        System.clearProperty("smtp.send.real");
    }

    @AfterEach
    void after() {
        System.clearProperty("smtp.send.real");
    }

    @Test
    void send_with_valid_data_does_not_throw_exception() {
        SmtpEmailSender sender = new SmtpEmailSender("user@gmail.com", "password", false);

        assertDoesNotThrow(() ->
            sender.send("from@test.com", "to@test.com", "Subject", "Body")
        );
    }

    @Test
    void send_throws_exception_when_credentials_missing() {
        SmtpEmailSender sender = new SmtpEmailSender(null, null, false);

        assertThrows(IllegalStateException.class, () ->
            sender.send("from@test.com", "to@test.com", "Subj", "Body")
        );
    }

    @Test
    void send_throws_exception_when_recipient_missing() {
        SmtpEmailSender sender = new SmtpEmailSender("user@gmail.com", "password", false);

        assertThrows(IllegalArgumentException.class, () ->
            sender.send("from@test.com", null, "Subj", "Body")
        );
    }

    @Test
    void company_email_checks() {
        assertEquals("remaajomaa842@gmail.com", SmtpEmailSender.getCompanyEmail());
        assertEquals(SmtpEmailSender.getCompanyEmail(), SmtpEmailSender.getEnvCompanyEmail());
    }
}