package persistence.dto;

import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) for serializing an {@link domain.Appointment} instance.
 * <p>
 * This DTO is used in persistence (e.g., JSON files) to transfer appointment data
 * between the in-memory model and disk storage.
 * </p>
 *
 * @author remaa
 * @version 1.0
 */
public class AppointmentDTO {

    /**
     * Unique appointment identifier.
     */
    public int id;

    /**
     * Username of the appointment owner.
     */
    public String username;

    /**
     * Appointment slot start date/time.
     */
    public LocalDateTime slotStart;

    /**
     * Appointment category name.
     */
    public String categoryName;

    /**
     * Appointment duration in minutes.
     */
    public int durationInMinutes;

    /**
     * Number of participants.
     */
    public int participants;

    /**
     * Appointment status as a string.
     */
    public String status;

    /**
     * Timestamp when the appointment was created.
     */
    public LocalDateTime createdAt;

    /**
     * Timestamp when the appointment was confirmed.
     */
    public LocalDateTime confirmedAt;

    /**
     * Timestamp when the appointment was cancelled.
     */
    public LocalDateTime cancelledAt;

    /**
     * Appointment type (can be null).
     */
    public String appointmentType;

    /**
     * Group size (if the appointment is a group).
     */
    public Integer groupSize;

    /**
     * Review appointment: target slot start time.
     */
    public LocalDateTime reviewTargetSlotStart;

    /**
     * Emergency appointment: preferred slot start time.
     */
    public LocalDateTime emergencyPreferredSlotStart;
}