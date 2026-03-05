package persistence.dto;

import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) representing a persisted snapshot of a contact request.
 *
 * <p>A contact request is a message sent from one user (identified by {@link #fromUsername})
 * to a provider (identified by {@link #toProviderUsername}).</p>
 *
 * <p>This DTO is designed for serialization/persistence and later restoration into the domain
 * {@code ContactRequest} object.</p>
 */
public class ContactRequestDTO {

    /**
     * Unique identifier of the contact request.
     */
    public int id;

    /**
     * Username of the sender.
     */
    public String fromUsername;

    /**
     * Username of the provider receiving the message.
     */
    public String toProviderUsername;

    /**
     * Message content.
     */
    public String message;

    /**
     * Timestamp indicating when the request was created.
     */
    public LocalDateTime createdAt;

    /**
     * Whether the provider has marked the request as read.
     */
    public boolean read;
}