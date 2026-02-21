package domain;

import java.time.LocalDateTime;

public class TimeSlot {

    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private boolean isBooked;

    public TimeSlot(LocalDateTime startDateTime, int durationInMinutes) {
        this.startDateTime = startDateTime;
        this.endDateTime = startDateTime.plusMinutes(durationInMinutes);
        this.isBooked = false;
    }

    public boolean isAvailable() {
        return !isBooked; 
    }

    public void book() {
        this.isBooked = true;
    }

    public void cancel() {
        this.isBooked = false;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }
}