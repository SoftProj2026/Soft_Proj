
package domain;

import java.time.LocalDateTime;

public class TimeSlot {

    private LocalDateTime startDateTime;
    private int duration;
    private boolean available;
    private Category category;

    public TimeSlot(LocalDateTime startDateTime, int duration, Category category) {
        this.startDateTime = startDateTime;
        this.duration = duration;
        this.category = category;
        this.available = true;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public int getDuration() {
        return duration;
    }

    public boolean isAvailable() {
        return available;
    }

    public Category getCategory() {
        return category;
    }

    public LocalDateTime getEndDateTime() {
        return startDateTime.plusMinutes(duration);
    }

    public void book() {
        this.available = false;
    }

    public void cancel() {
        this.available = true;
    }
}