package Test;

import Service.FakeEmailSender;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FakeEmailSenderAdditionalTest {

    @Test
    void collectsMultipleSentEmails() {
        FakeEmailSender fake = new FakeEmailSender();
        fake.send("from1@example.com", "to1@example.com", "subject1", "body1");
        fake.send("from2@example.com", "to2@example.com", "subject2", "body2");
        assertNotNull(fake.sent);
        assertEquals(2, fake.sent.size());
        assertEquals("to1@example.com", fake.sent.get(0).to);
        assertEquals("to2@example.com", fake.sent.get(1).to);
    }
}