package persistence.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * JSON-friendly snapshot of repository state with no object cycles.
 *
 * <p>
 * This DTO represents the persisted form of the application's DataRepository.
 * It contains serializable lists of all core entities and necessary counter values for restoring static IDs.
 * </p>
 *
 * @author Qussaialaw
 * @version 1.0
 */
public class RepoSnapshot {

    /**
     * Next available appointment ID.
     */
    public int nextAppointmentId = 1;

    /**
     * Next available audit event ID.
     */
    public int nextAuditEventId = 1;

    /**
     * Next available booking request ID.
     */
    public int nextBookingRequestId = 1;

    /**
     * Next available contact request ID.
     */
    public int nextContactRequestId = 1;

    /**
     * All serialized users.
     */
    public List<UserDTO> users = new ArrayList<>();

    /**
     * All serialized providers.
     */
    public List<ProviderDTO> providers = new ArrayList<>();

    /**
     * All serialized categories.
     */
    public List<CategoryDTO> categories = new ArrayList<>();

    /**
     * All serialized time slots.
     */
    public List<TimeSlotDTO> slots = new ArrayList<>();

    /**
     * All serialized appointments.
     */
    public List<AppointmentDTO> appointments = new ArrayList<>();

    /**
     * All serialized contact requests.
     */
    public List<ContactRequestDTO> contactRequests = new ArrayList<>();

    /**
     * All serialized audit events.
     */
    public List<AuditEventDTO> auditEvents = new ArrayList<>();

    /**
     * All serialized booking requests.
     */
    public List<BookingRequestDTO> bookingRequests = new ArrayList<>();

    /**
     * All (username, category, used) cancellation records.
     */
    public List<CancelUsedDTO> cancelUsed = new ArrayList<>();
}