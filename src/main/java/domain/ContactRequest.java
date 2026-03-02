package domain;

import java.time.LocalDateTime;

/**
 * Represents a contact request (message) sent by a customer to a provider.
 * <p>
 * This is used for internal in-app messaging:
 * <ul>
 *   <li>Customer sends a message to a {@link Provider}</li>
 *   <li>Provider views messages in inbox</li>
 *   <li>Admin can also review messages (via repository/admin screens)</li>
 * </ul>
 * </p>
 */
public class ContactRequest {

    /** Counter for generating unique request IDs. */
    private static int counter = 1;

    private final int id;
    private final String fromUsername;
    private final String toProviderUsername;
    private final String message;
    private final LocalDateTime createdAt;

    /** True if the provider has marked this message as read. */
    private boolean read;

    /**
     * Creates a new contact request and sets {@link #createdAt} to now.
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
     * Returns request id.
     *
     * @return request id
     */
    public int getId() {
        return id;
    }

    /**
     * Returns sender username.
     *
     * @return from username
     */
    public String getFromUsername() {
        return fromUsername;
    }

    /**
     * Returns recipient provider username.
     *
     * @return to provider username
     */
    public String getToProviderUsername() {
        return toProviderUsername;
    }

    /**
     * Returns message text.
     *
     * @return message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns message creation time.
     *
     * @return createdAt timestamp
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Indicates whether the message was marked as read.
     *
     * @return true if read
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