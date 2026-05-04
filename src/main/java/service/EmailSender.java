package service;

/**
 * Abstraction for sending email messages.
 *
 * <p>This interface allows the application to send email through different implementations, such as:</p>
 * <ul>
 *   <li>SMTP-based sending (production)</li>
 *   <li>Console/logging-based sending (development)</li>
 *   <li>Mock/fake implementations (unit tests)</li>
 * </ul>
 * @author remaa
 * @version 1.0
 */
public interface EmailSender {

    /**
     * Sends an email message.
     *
     * @param from    sender email address
     * @param to      recipient email address
     * @param subject email subject line
     * @param body    email body text
     */
    void send(String from, String to, String subject, String body);
}