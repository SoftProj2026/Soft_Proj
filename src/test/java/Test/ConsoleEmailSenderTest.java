package Test;

import org.junit.jupiter.api.Test;

import service.ConsoleEmailSender;

class ConsoleEmailSenderTest {

    @Test
    void testSendLogsToConsole() {
        ConsoleEmailSender sender = new ConsoleEmailSender();
        String from = "from@example.com";
        String to = "to@example.com";
        String subject = "Test Email";
        String body = "Hello from test!";

        sender.send(from, to, subject, body);
    }
}