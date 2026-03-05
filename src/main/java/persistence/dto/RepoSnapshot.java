package persistence.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * JSON-friendly snapshot of repository state with no object cycles.
 *
 * <p>This DTO represents the persisted form of the application's {@code DataRepository}.
 * It stores domain entities in flat list form and uses primitive fields / strings instead of
 * direct object references so it can be serialized safely.</p>
 *
 * <p>The {@code next*Id} fields are used to restore static/incrementing ID counters in domain
 * classes when reloading persisted data.</p>
 */
public class RepoSnapshot {

    /**
     * Next appointment ID to assign when creating a new appointment.
     */
    public int nextAppointmentId = 1;

    /**
     * Next audit event ID to assign when creating a new audit event.
     */
    public int nextAuditEventId = 1;

    /**
     * Next booking request ID to assign when creating a new booking request.
     */
    public int nextBookingRequestId = 1;

    /**
     * Next contact request ID to assign when creating a new contact request.
     */
    public int nextContactRequestId = 1;

    /**
     * All users (including providers and administrators) stored as {@link UserDTO}.
     */
    public List<UserDTO> users = new ArrayList<>();

    /**
     * Optional separate list of providers, used to reconstruct the repository's provider collection.
     */
    public List<ProviderDTO> providers = new ArrayList<>();

    /**
     * All categories available in the system.
     */
    public List<CategoryDTO> categories = new ArrayList<>();

    /**
     * All time slots available in the system.
     */
    public List<TimeSlotDTO> slots = new ArrayList<>();

    /**
     * All appointments stored in the system.
     */
    public List<AppointmentDTO> appointments = new ArrayList<>();

    /**
     * All contact requests/messages sent to providers.
     */
    public List<ContactRequestDTO> contactRequests = new ArrayList<>();

    /**
     * All audit events recorded by the system.
     */
    public List<AuditEventDTO> auditEvents = new ArrayList<>();

    /**
     * All booking requests stored in the system.
     */
    public List<BookingRequestDTO> bookingRequests = new ArrayList<>();

    /**
     * Records indicating whether a user has used a cancellation privilege for a given category.
     */
    public List<CancelUsedDTO> cancelUsed = new ArrayList<>();
}