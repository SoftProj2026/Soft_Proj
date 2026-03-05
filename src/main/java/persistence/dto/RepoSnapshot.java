package persistence.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * JSON-friendly snapshot of repository state (no object cycles).
 */
public class RepoSnapshot {

    public int nextAppointmentId = 1;
    public int nextAuditEventId = 1;
    public int nextBookingRequestId = 1;
    public int nextContactRequestId = 1;

    public List<UserDTO> users = new ArrayList<>();
    public List<ProviderDTO> providers = new ArrayList<>();
    public List<CategoryDTO> categories = new ArrayList<>();
    public List<TimeSlotDTO> slots = new ArrayList<>();
    public List<AppointmentDTO> appointments = new ArrayList<>();
    public List<ContactRequestDTO> contactRequests = new ArrayList<>();
    public List<AuditEventDTO> auditEvents = new ArrayList<>();
    public List<BookingRequestDTO> bookingRequests = new ArrayList<>();

    public List<CancelUsedDTO> cancelUsed = new ArrayList<>();
}