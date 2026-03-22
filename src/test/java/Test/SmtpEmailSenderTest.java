package Test;

import Service.SmtpEmailSender;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SmtpEmailSenderTest {

    @Test
    void missingConfiguration_throws() {
        SmtpEmailSender sender = new SmtpEmailSender(); 
        Exception ex = assertThrows(IllegalStateException.class, () -> {
            sender.send("from@example.com", "to@example.com", "subject", "body");
        });
        assertTrue(ex.getMessage().toLowerCase().contains("smtp") || ex.getMessage().toLowerCase().contains("configuration"));
    }
}