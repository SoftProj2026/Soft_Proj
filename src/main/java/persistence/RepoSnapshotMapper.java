package persistence;

import domain.*;
import persistence.dto.*;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class responsible for mapping between an in-memory {@link DataRepository} instance and a serializable
 * {@link RepoSnapshot} DTO.
 *
 * <p>This mapper performs a deep conversion of repository state into DTOs suitable for persistence (e.g., JSON),
 * and reconstructs domain objects back into a repository instance.</p>
 *
 * <p>In addition to mapping entities, it also restores internal/static ID counters and certain repository-private
 * state using reflection.</p>
 *
 * @author Qussaialaw
 * @version 1.0
 */
public final class RepoSnapshotMapper {

    /**
     * Prevents instantiation; this class exposes only static utility methods.
     */
    private RepoSnapshotMapper() {}

    /**
     * Creates a {@link RepoSnapshot} DTO from the given {@link DataRepository}.
     *
     * <p>The snapshot includes:
     * <ul>
     *   <li>All categories, users, providers, slots, appointments, contact requests, audit events, booking requests</li>
     *   <li>Cancellation usage map (user + category)</li>
     *   <li>Next ID counter values computed from the maximum existing IDs</li>
     * </ul>
     * @param repo the source repository to snapshot
     * @return a snapshot representing the current repository state
     */
    public static RepoSnapshot fromRepo(DataRepository repo) {
        RepoSnapshot s = new RepoSnapshot();

        s.nextAppointmentId = repo.getAppointments().stream().mapToInt(Appointment::getId).max().orElse(0) + 1;
        s.nextAuditEventId = repo.getAuditEvents().stream().mapToInt(AuditEvent::getId).max().orElse(0) + 1;
        s.nextBookingRequestId = repo.getBookingRequests().stream().mapToInt(BookingRequest::getId).max().orElse(0) + 1;
        s.nextContactRequestId = repo.getContactRequests().stream().mapToInt(ContactRequest::getId).max().orElse(0) + 1;

        for (Category c : repo.getCategories()) {
            CategoryDTO dto = new CategoryDTO();
            dto.name = c.getName();
            s.categories.add(dto);
        }

        for (User u : repo.getUsers()) {
            UserDTO dto = new UserDTO();
            dto.username = u.getUsername();
            dto.password = u.getPassword();
            dto.firstName = u.getFirstName();
            dto.lastName = u.getLastName();
            dto.dateOfBirth = u.getDateOfBirth();
            dto.email = u.getEmail();

            if (u instanceof Provider) {
                Provider p = (Provider) u;
                dto.type = "Provider";
                dto.displayName = p.getDisplayName();
                dto.phone = p.getPhone();
                dto.email = p.getEmail();
                dto.address = p.getAddress();
            } else if (u instanceof Administrator) {
                dto.type = "Administrator";
            } else {
                dto.type = "User";
            }
            s.users.add(dto);
        }

        for (Provider p : repo.getProviders()) {
            ProviderDTO dto = new ProviderDTO();
            dto.username = p.getUsername();
            dto.password = p.getPassword();
            dto.displayName = p.getDisplayName();
            dto.phone = p.getPhone();
            dto.email = p.getEmail();
            dto.address = p.getAddress();
            s.providers.add(dto);
        }

        for (TimeSlot slot : repo.getSlots()) {
            TimeSlotDTO dto = new TimeSlotDTO();
            dto.start = slot.getStartDateTime();
            dto.durationMinutes = (int) Duration.between(slot.getStartDateTime(), slot.getEndDateTime()).toMinutes();
            dto.categoryName = (slot.getCategory() != null) ? slot.getCategory().getName() : null;

            dto.booked = !slot.isAvailable() && !slot.isHeld();
            dto.held = slot.isHeld();
            dto.heldRequestId = slot.getHeldRequestId();

            if (!dto.held) dto.booked = !slot.isAvailable();

            s.slots.add(dto);
        }

        for (Appointment a : repo.getAppointments()) {
            AppointmentDTO dto = new AppointmentDTO();
            dto.id = a.getId();
            dto.username = (a.getUser() != null) ? a.getUser().getUsername() : null;
            dto.slotStart = (a.getSlot() != null) ? a.getSlot().getStartDateTime() : null;
            dto.categoryName = (a.getSlot() != null && a.getSlot().getCategory() != null) ? a.getSlot().getCategory().getName() : null;

            dto.durationInMinutes = a.getDurationInMinutes();
            dto.participants = a.getParticipants();
            dto.status = a.getStatus().name();

            dto.createdAt = a.getCreatedAt();
            dto.confirmedAt = a.getConfirmedAt();
            dto.cancelledAt = a.getCancelledAt();

            dto.appointmentType = (a.getAppointmentType() != null) ? a.getAppointmentType().name() : null;
            dto.groupSize = a.getGroupSize();
            dto.reviewTargetSlotStart = a.getReviewTargetSlotStart();
            dto.emergencyPreferredSlotStart = a.getEmergencyPreferredSlotStart();

            s.appointments.add(dto);
        }

        for (ContactRequest r : repo.getContactRequests()) {
            ContactRequestDTO dto = new ContactRequestDTO();
            dto.id = r.getId();
            dto.fromUsername = r.getFromUsername();
            dto.toProviderUsername = r.getToProviderUsername();
            dto.message = r.getMessage();
            dto.createdAt = r.getCreatedAt();
            dto.read = r.isRead();
            s.contactRequests.add(dto);
        }

        for (AuditEvent e : repo.getAuditEvents()) {
            AuditEventDTO dto = new AuditEventDTO();
            dto.id = e.getId();
            dto.type = e.getType().name();
            dto.actorUsername = e.getActorUsername();
            dto.target = e.getTarget();
            dto.details = e.getDetails();
            dto.at = e.getAt();
            s.auditEvents.add(dto);
        }

        for (BookingRequest r : repo.getBookingRequests()) {
            BookingRequestDTO dto = new BookingRequestDTO();
            dto.id = r.getId();
            dto.requesterUsername = (r.getRequester() != null) ? r.getRequester().getUsername() : null;
            dto.slotStart = (r.getSlot() != null) ? r.getSlot().getStartDateTime() : null;
            dto.categoryName = (r.getSlot() != null && r.getSlot().getCategory() != null) ? r.getSlot().getCategory().getName() : null;

            dto.durationInMinutes = r.getDurationInMinutes();
            dto.participants = r.getParticipants();
            dto.categoryAdminUsername = r.getCategoryAdminUsername();
            dto.status = r.getStatus().name();

            dto.createdAt = r.getCreatedAt();
            dto.categoryDecisionAt = r.getCategoryDecisionAt();
            dto.bigAdminDecisionAt = r.getBigAdminDecisionAt();
            dto.categoryAdminActor = r.getCategoryAdminActor();
            dto.bigAdminActor = r.getBigAdminActor();
            dto.rejectReason = r.getRejectReason();

            s.bookingRequests.add(dto);
        }

        Map<String, Boolean> cancelMap = readCancelMap(repo);
        for (Map.Entry<String, Boolean> en : cancelMap.entrySet()) {
            String key = en.getKey();
            String[] parts = key.split("\\|", 2);
            CancelUsedDTO dto = new CancelUsedDTO();
            dto.username = parts.length > 0 ? parts[0] : "";
            dto.categoryName = parts.length > 1 ? parts[1] : "";
            dto.used = Boolean.TRUE.equals(en.getValue());
            s.cancelUsed.add(dto);
        }

        return s;
    }

    /**
     * Reconstructs a {@link DataRepository} from a previously created {@link RepoSnapshot}.
     *
     * @param s the snapshot to restore from
     * @return a reconstructed repository instance
     */
    public static DataRepository toRepo(RepoSnapshot s) {
        DataRepository repo = new DataRepository();

        Map<String, Category> catByName = new HashMap<>();
        for (CategoryDTO c : s.categories) {
            Category cat = new Category(c.name);
            repo.addCategory(cat);
            catByName.put(norm(c.name), cat);
        }

        Map<String, User> userByUsername = new HashMap<>();
        for (UserDTO u : s.users) {
            User created;
            String type = u.type != null ? u.type : "User";

            if ("Provider".equalsIgnoreCase(type)) {
                created = new Provider(
                        u.username, u.password,
                        u.displayName, u.phone, u.email, u.address
                );
                created.setEmail(u.email);
                repo.addUser(created);
            } else if ("Administrator".equalsIgnoreCase(type)) {
                created = new Administrator(u.username, u.password);
                created.setEmail(u.email);
                repo.addUser(created);
            } else {
                created = new User(u.firstName, u.lastName, u.username, u.password, u.dateOfBirth, u.email);
                repo.addUser(created);
            }

            userByUsername.put(norm(u.username), created);
        }

        for (ProviderDTO p : s.providers) {
            User u = userByUsername.get(norm(p.username));
            if (u instanceof Provider) {
                repo.getProviders().add((Provider) u);
            } else {
                Provider created = new Provider(
                        p.username, p.password,
                        p.displayName, p.phone, p.email, p.address
                );
                repo.addProvider(created);
                userByUsername.put(norm(p.username), created);
            }
        }

        Map<String, TimeSlot> slotByKey = new HashMap<>();
        for (TimeSlotDTO dto : s.slots) {
            Category cat = catByName.get(norm(dto.categoryName));
            TimeSlot slot = new TimeSlot(dto.start, dto.durationMinutes, cat);

            if (dto.booked) slot.book();
            if (dto.held) slot.hold(dto.heldRequestId != null ? dto.heldRequestId : -1);

            repo.addSlot(slot);

            slotByKey.put(slotKey(dto.categoryName, dto.start), slot);
        }

        for (BookingRequestDTO dto : s.bookingRequests) {
            User requester = userByUsername.get(norm(dto.requesterUsername));
            TimeSlot slot = slotByKey.get(slotKey(dto.categoryName, dto.slotStart));

            if (requester == null || slot == null) continue;

            BookingRequest r = new BookingRequest(
                    requester,
                    slot,
                    dto.durationInMinutes,
                    dto.participants,
                    dto.categoryAdminUsername
            );

            forceBookingRequestState(r, dto);

            repo.addBookingRequest(r);
        }

        for (AppointmentDTO dto : s.appointments) {
            User user = userByUsername.get(norm(dto.username));
            TimeSlot slot = slotByKey.get(slotKey(dto.categoryName, dto.slotStart));
            if (user == null || slot == null) continue;

            Appointment a = new Appointment(user, slot, dto.durationInMinutes, dto.participants);
            forceAppointmentState(a, dto);

            repo.addAppointment(a);
        }

        for (ContactRequestDTO dto : s.contactRequests) {
            ContactRequest r = new ContactRequest(dto.fromUsername, dto.toProviderUsername, dto.message);
            forceContactRequestState(r, dto);
            repo.getContactRequests().add(r);
        }

        for (AuditEventDTO dto : s.auditEvents) {
            AuditEvent e = new AuditEvent(
                    AuditEvent.Type.valueOf(dto.type),
                    dto.actorUsername,
                    dto.target,
                    dto.details
            );
            forceAuditEventState(e, dto);
            repo.getAuditEvents().add(e);
        }

        Map<String, Boolean> cancelMap = new HashMap<>();
        for (CancelUsedDTO dto : s.cancelUsed) {
            String key = (dto.username == null ? "" : dto.username.trim().toLowerCase())
                    + "|"
                    + (dto.categoryName == null ? "" : dto.categoryName.trim().toLowerCase());
            cancelMap.put(key, dto.used);
        }
        writeCancelMap(repo, cancelMap);

        setStaticCounter(domain.Appointment.class, "counter", s.nextAppointmentId);
        setStaticCounter(domain.AuditEvent.class, "counter", s.nextAuditEventId);
        setStaticCounter(domain.BookingRequest.class, "counter", s.nextBookingRequestId);
        setStaticCounter(domain.ContactRequest.class, "counter", s.nextContactRequestId);

        return repo;
    }

    /**
     * Normalizes a string for use as a map key or comparison.
     *
     * @param s string
     * @return normalized string (lowercase, trimmed, or "" if null)
     */
    private static String norm(String s) {
        return s == null ? "" : s.trim().toLowerCase();
    }

    /**
     * Constructs a composite key identifying a slot in serialization.
     *
     * @param categoryName category name
     * @param start        slot start datetime
     * @return key string
     */
    private static String slotKey(String categoryName, java.time.LocalDateTime start) {
        return norm(categoryName) + "|" + (start != null ? start.toString() : "");
    }

    /**
     * Reads the cancel-used map using reflection.
     *
     * @param repo repository
     * @return cancel map copy
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Boolean> readCancelMap(DataRepository repo) {
        try {
            Field f = DataRepository.class.getDeclaredField("cancelUsedByUserCategory");
            f.setAccessible(true);
            Object v = f.get(repo);
            if (v instanceof Map) {
                return new HashMap<>((Map<String, Boolean>) v);
            }
        } catch (Exception ignored) {}
        return new HashMap<>();
    }

    /**
     * Writes the cancel-used map using reflection.
     *
     * @param repo repository
     * @param map  cancel-used state
     */
    private static void writeCancelMap(DataRepository repo, Map<String, Boolean> map) {
        try {
            Field f = DataRepository.class.getDeclaredField("cancelUsedByUserCategory");
            f.setAccessible(true);
            f.set(repo, map);
        } catch (Exception ignored) {}
    }

    /**
     * Sets a static counter field for restoring IDs.
     *
     * @param cls       class with counter
     * @param field     field name
     * @param nextValue new value
     */
    private static void setStaticCounter(Class<?> cls, String field, int nextValue) {
        try {
            Field f = cls.getDeclaredField(field);
            f.setAccessible(true);
            f.setInt(null, nextValue);
        } catch (Exception ignored) {}
    }

    /**
     * Restores appointment state from its DTO.
     *
     * @param a   appointment
     * @param dto source DTO
     */
    private static void forceAppointmentState(Appointment a, AppointmentDTO dto) {
        try {
            setField(a, "status", AppointmentStatus.valueOf(dto.status));
            setField(a, "confirmedAt", dto.confirmedAt);
            setField(a, "cancelledAt", dto.cancelledAt);
        } catch (Exception ignored) {}

        try {
            if (dto.appointmentType != null && !dto.appointmentType.trim().isEmpty()) {
                a.setAppointmentType(AppointmentType.valueOf(dto.appointmentType.trim()));
            }
            a.setGroupSize(dto.groupSize);
            a.setReviewTargetSlotStart(dto.reviewTargetSlotStart);
            a.setEmergencyPreferredSlotStart(dto.emergencyPreferredSlotStart);
        } catch (Exception ignored) {}
    }

    /**
     * Restores booking request state from its DTO.
     *
     * @param r   booking request
     * @param dto source DTO
     */
    private static void forceBookingRequestState(BookingRequest r, BookingRequestDTO dto) {
        try {
            setField(r, "status", BookingRequestStatus.valueOf(dto.status));
            setField(r, "categoryDecisionAt", dto.categoryDecisionAt);
            setField(r, "bigAdminDecisionAt", dto.bigAdminDecisionAt);
            setField(r, "categoryAdminActor", dto.categoryAdminActor);
            setField(r, "bigAdminActor", dto.bigAdminActor);
            setField(r, "rejectReason", dto.rejectReason);
        } catch (Exception ignored) {}
    }

    /**
     * Restores contact request state from its DTO.
     *
     * @param r   contact request
     * @param dto source DTO
     */
    private static void forceContactRequestState(ContactRequest r, ContactRequestDTO dto) {
        try {
            setField(r, "read", dto.read);
        } catch (Exception ignored) {}
    }

    /**
     * Restores audit event state from its DTO.
     *
     * @param e   audit event
     * @param dto source DTO
     */
    private static void forceAuditEventState(AuditEvent e, AuditEventDTO dto) {
        try {
        } catch (Exception ignored) {}
    }

    /**
     * Sets a private field using reflection.
     *
     * @param obj  object
     * @param name field name
     * @param value value to set
     * @throws Exception reflection error
     */
    private static void setField(Object obj, String name, Object value) throws Exception {
        Field f = obj.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(obj, value);
    }
}