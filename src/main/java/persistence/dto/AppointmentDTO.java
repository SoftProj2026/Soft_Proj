package persistence.dto;

import java.time.LocalDateTime;

/**
 * Data transfer object representing an appointment for persistence/serialization.
 *
 * <p>This DTO is used inside repository snapshots to store appointment data in a JSON-friendly structure.</p>
 */
public class AppointmentDTO {

    /** Appointment identifier as stored in the snapshot. */
    public int id;

    /** Username of the appointment owner. */
    public String username;

    /** Start date/time of the associated slot. */
    public LocalDateTime slotStart;

    /** Category name associated with the appointment. */
    public String categoryName;

    /** Appointment duration in minutes. */
    public int durationInMinutes;

    /** Number of participants for the appointment. */
    public int participants;

    /**
     * Appointment status stored as text.
     *
     * <p>Expected values: {@code "PENDING"}, {@code "CONFIRMED"}, {@code "CANCELLED"}.</p>
     */
    public String status;

    /** Timestamp when the appointment was created. */
    public LocalDateTime createdAt;

    /** Timestamp when the appointment was confirmed, if applicable. */
    public LocalDateTime confirmedAt;

    /** Timestamp when the appointment was cancelled, if applicable. */
    public LocalDateTime cancelledAt;
}