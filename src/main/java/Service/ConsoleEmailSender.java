package Service;

public class ConsoleEmailSender implements EmailSender {
    @Override
    public void send(String from, String to, String subject, String body) {
        System.out.println("[EMAIL] From: " + from);
        System.out.println("[EMAIL] To: " + to);
        System.out.println("[EMAIL] Subject: " + subject);
        System.out.println("[EMAIL] Body:\n" + body);
        System.out.println("--------------------------------------------------");
    }
}