package domain;

/**
 * Represents a booking appointment made by a {@link User} for a specific {@link TimeSlot}.
 *
 * <p>Each appointment is assigned a unique auto-incremented ID upon creation
 * and starts with a {@link AppointmentStatus#PENDING} status.</p>
 */
public class Appointment {

    private static int counter = 1;

    private int id;
    private User user;
    private TimeSlot slot;
    private int durationInMinutes;
    private int participants;
    private AppointmentStatus status;

    /**
     * Constructs a new Appointment with the given details.
     * The appointment's status is initially set to {@link AppointmentStatus#PENDING}.
     *
     * @param user              the user who is making the appointment
     * @param slot              the time slot reserved for the appointment
     * @param durationInMinutes the duration of the appointment in minutes
     * @param participants      the number of participants attending
     */
    public Appointment(User user, TimeSlot slot,
                       int durationInMinutes, int participants) {

        this.id = counter++;
        this.user = user;
        this.slot = slot;
        this.durationInMinutes = durationInMinutes;
        this.participants = participants;
        this.status = AppointmentStatus.PENDING;
    }

    /**
     * Returns the unique identifier of this appointment.
     *
     * @return the appointment ID
     */
    public int getId() { return id; }

    /**
     * Returns the user who made this appointment.
     *
     * @return the associated {@link User}
     */
    public User getUser() { return user; }

    /**
     * Returns the time slot reserved for this appointment.
     *
     * @return the associated {@link TimeSlot}
     */
    public TimeSlot getSlot() { return slot; }

    /**
     * Returns the duration of the appointment in minutes.
     *
     * @return the duration in minutes
     */
    public int getDurationInMinutes() { return durationInMinutes; }

    /**
     * Returns the number of participants attending the appointment.
     *
     * @return the number of participants
     */
    public int getParticipants() { return participants; }

    /**
     * Returns the current status of the appointment.
     *
     * @return the {@link AppointmentStatus}
     */
    public AppointmentStatus getStatus() { return status; }

    /**
     * Confirms the appointment, changes its status to {@link AppointmentStatus#CONFIRMED},
     * and marks the associated time slot as booked.
     * US2.1 - Confirm booking and mark slot as unavailable.
     */
    public void confirm() {
        this.status = AppointmentStatus.CONFIRMED;
        slot.book();
    }

    /**
     * Cancels the appointment, changes its status to {@link AppointmentStatus#CANCELLED},
     * and releases the associated time slot.
     */
    public void cancel() {
        this.status = AppointmentStatus.CANCELLED;
        slot.cancel();
    }
}