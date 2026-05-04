package service;

import java.util.ArrayList;
import java.util.List;

/**
 * In-memory {@link EmailSender} implementation for testing.
 *
 * <p>This sender does not deliver emails. Instead, it stores each sent email in-memory so tests
 * can assert the number of messages and their contents.</p>
 * @auther Qussai
 * @version 1.0
 */
public class FakeEmailSender implements EmailSender {

    /**
     * Represents one captured outgoing email.
     */
    public static class SentEmail {

        /**
         * Sender email address.
         */
        public final String from;

        /**
         * Recipient email address.
         */
        public final String to;

        /**
         * Email subject line.
         */
        public final String subject;

        /**
         * Email body text.
         */
        public final String body;

        /**
         * Creates a captured email record.
         *
         * @param from    sender email address
         * @param to      recipient email address
         * @param subject email subject
         * @param body    email body
         */
        public SentEmail(String from, String to, String subject, String body) {
            this.from = from;
            this.to = to;
            this.subject = subject;
            this.body = body;
        }
    }

    /**
     * List of emails captured by this fake sender.
     */
    public final List<SentEmail> sent = new ArrayList<>();

    /**
     * Captures an email by storing it in {@link #sent}.
     *
     * @param from    sender email address
     * @param to      recipient email address
     * @param subject email subject line
     * @param body    email body text
     */
    @Override
    public void send(String from, String to, String subject, String body) {
        sent.add(new SentEmail(from, to, subject, body));
    }
}