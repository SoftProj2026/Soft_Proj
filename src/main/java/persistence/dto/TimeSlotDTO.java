package persistence.dto;

import java.time.LocalDateTime;

public class TimeSlotDTO {
    public LocalDateTime start;
    public int durationMinutes;
    public String categoryName;

    public boolean booked;
    public boolean held;
    public Integer heldRequestId;
}