package domain; 
public class Appointment {

    private static int counter = 1;

    private int id;
    private User user;
    private TimeSlot slot;
    private int durationInMinutes;
    private int participants;
    private AppointmentStatus status;

    public Appointment(User user, TimeSlot slot, int durationInMinutes, int participants) {
        this.id = counter++;
        this.user = user;
        this.slot = slot;
        this.durationInMinutes = durationInMinutes;
        this.participants = participants;
        this.status = AppointmentStatus.PENDING;
    }

    public int getId() { return id; }
    public User getUser() { return user; }
    public TimeSlot getSlot() { return slot; }
    public int getDurationInMinutes() { return durationInMinutes; }
    public int getParticipants() { return participants; }
    public AppointmentStatus getStatus() { return status; }

    public void confirm() {
        this.status = AppointmentStatus.CONFIRMED;
        slot.book();
    }

    public void cancel() {
        this.status = AppointmentStatus.CANCELLED;
        slot.cancel();
    }
}
