package Test;

import org.junit.jupiter.api.Test;

import service.EmailSender;
import service.FixedEmailService;

import static org.mockito.Mockito.*;

class FixedEmailServiceTest {

    @Test
    void testSendToFixedUserCallsSenderWithCorrectParams() {
        EmailSender mockSender = mock(EmailSender.class);
        FixedEmailService service = new FixedEmailService(mockSender);

        String expectedSubject = "Hello";
        String expectedBody = "JUnit Coverage Test!";

        service.sendToFixedUser(expectedSubject, expectedBody);

        verify(mockSender, times(1)).send(
            "remaajomaa842@gmail.com", 
            "remaajomaa70@gmail.com",  
            expectedSubject,
            expectedBody
        );
    }
}