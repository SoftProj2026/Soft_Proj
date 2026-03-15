package domain;

import java.time.LocalDateTime;

/**
 * Represents a contact request (message) sent by a customer to a provider.
 *
 * <p>This class is used for internal in-app messaging:</p>
 * <ul>
 *   <li>A customer sends a message to a {@link Provider}.</li>
 *   <li>The provider views messages in their inbox.</li>
 *   <li>An administrator can review messages through admin/repository screens.</li>
 * </ul>
 *
 * @author Qussaialaw
 * @version 1.0
 */
public class ContactRequest {

    /**
     * Static counter used to generate unique request identifiers.
     */
    private static int counter = 1;

    /**
     * Unique contact request identifier.
     */
    private final int id;

    /**
     * Username of the sender (customer).
     */
    private final String fromUsername;

    /**
     * Username of the recipient provider.
     */
    private final String toProviderUsername;

    /**
     * Message content.
     */
    private final String message;

    /**
     * Timestamp when the request was created.
     */
    private final LocalDateTime createdAt;

    /**
     * Indicates whether the provider has marked this message as read.
     */
    private boolean read;

    /**
     * Creates a new contact request and sets the creation time to now.
     *
     * @param fromUsername       sender username (customer)
     * @param toProviderUsername recipient provider username
     * @param message            message text
     */
    public ContactRequest(String fromUsername, String toProviderUsername, String message) {
        this.id = counter++;
        this.fromUsername = fromUsername != null ? fromUsername.trim() : "";
        this.toProviderUsername = toProviderUsername != null ? toProviderUsername.trim() : "";
        this.message = message != null ? message.trim() : "";
        this.createdAt = LocalDateTime.now();
        this.read = false;
    }

    /**
     * Returns the contact request identifier.
     *
     * @return request id
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the sender username.
     *
     * @return sender username
     */
    public String getFromUsername() {
        return fromUsername;
    }

    /**
     * Returns the recipient provider username.
     *
     * @return provider username
     */
    public String getToProviderUsername() {
        return toProviderUsername;
    }

    /**
     * Returns the message content.
     *
     * @return message text
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns the creation timestamp of this message.
     *
     * @return created at timestamp
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Indicates whether this message was marked as read.
     *
     * @return {@code true} if read; otherwise {@code false}
     */
    public boolean isRead() {
        return read;
    }

    /**
     * Marks this request as read.
     */
    public void markRead() {
        this.read = true;
    }
}