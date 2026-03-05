package persistence.dto;

import java.time.LocalDateTime;

public class BookingRequestDTO {
    public int id;

    public String requesterUsername;
    public LocalDateTime slotStart;
    public String categoryName;

    public int durationInMinutes;
    public int participants;

    public String categoryAdminUsername;
    public String status;

    public LocalDateTime createdAt;
    public LocalDateTime categoryDecisionAt;
    public LocalDateTime bigAdminDecisionAt;

    public String categoryAdminActor;
    public String bigAdminActor;

    public String rejectReason;
}