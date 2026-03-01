package domain;

/**
 * Represents a booking appointment made by a user for a specific {@link TimeSlot}.
 * <p>
 * Each appointment has a unique incremental ID, a user, a slot, duration,
 * number of participants, and a status.
 * </p>
 */
public class Appointment {

    /** Counter used to generate auto-increment appointment IDs. */
    private static int counter = 1;

    private final int id;
    private final User user;
    private final TimeSlot slot;
    private final int durationInMinutes;
    private final int participants;
    private AppointmentStatus status;

    /**
     * Creates a new appointment in {@link AppointmentStatus#PENDING} state.
     *
     * @param user              the user who is making the appointment
     * @param slot              the selected time slot
     * @param durationInMinutes appointment duration in minutes
     * @param participants      number of participants
     */
    public Appointment(User user, TimeSlot slot, int durationInMinutes, int participants) {
        this.id = counter++;
        this.user = user;
        this.slot = slot;
        this.durationInMinutes = durationInMinutes;
        this.participants = participants;
        this.status = AppointmentStatus.PENDING;
    }

    /**
     * Returns the unique appointment ID.
     *
     * @return appointment ID
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the user who owns this appointment.
     *
     * @return the appointment owner
     */
    public User getUser() {
        return user;
    }

    /**
     * Returns the booked time slot.
     *
     * @return the time slot associated with this appointment
     */
    public TimeSlot getSlot() {
        return slot;
    }

    /**
     * Returns the appointment duration.
     *
     * @return duration in minutes
     */
    public int getDurationInMinutes() {
        return durationInMinutes;
    }

    /**
     * Returns the number of participants.
     *
     * @return participants count
     */
    public int getParticipants() {
        return participants;
    }

    /**
     * Returns the current appointment status.
     *
     * @return appointment status
     */
    public AppointmentStatus getStatus() {
        return status;
    }

    /**
     * Confirms this appointment and marks its slot as booked/unavailable.
     * <p>
     * This transitions the appointment to {@link AppointmentStatus#CONFIRMED}
     * and calls {@link TimeSlot#book()}.
     * </p>
     */
    public void confirm() {
        this.status = AppointmentStatus.CONFIRMED;
        slot.book();
    }

    /**
     * Cancels this appointment and releases its slot (makes it available again).
     * <p>
     * This transitions the appointment to {@link AppointmentStatus#CANCELLED}
     * and calls {@link TimeSlot#cancel()}.
     * </p>
     */
    public void cancel() {
        this.status = AppointmentStatus.CANCELLED;
        slot.cancel();
    }
}