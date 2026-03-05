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
     * </p>
     *
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
     * <p>This method restores:
     * <ul>
     *   <li>All domain objects (categories, users/providers, slots, requests, appointments, events)</li>
     *   <li>Slot state (booked/held)</li>
     *   <li>Request/appointment states using reflection</li>
     *   <li>Repository private state for cancellation usage</li>
     *   <li>Static ID counters for multiple domain classes</li>
     * </ul>
     * </p>
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
                repo.addUser(created);
            } else if ("Administrator".equalsIgnoreCase(type)) {
                created = new Administrator(u.username, u.password);
                repo.addUser(created);
            } else {
                created = new User(u.firstName, u.lastName, u.username, u.password, u.dateOfBirth);
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
     * Normalizes a string for use as a map key by trimming and lower-casing.
     *
     * @param s input string (may be {@code null})
     * @return normalized string, or empty string if {@code s} is {@code null}
     */
    private static String norm(String s) {
        return s == null ? "" : s.trim().toLowerCase();
    }

    /**
     * Creates a stable key for slot lookup based on category name and slot start time.
     *
     * @param categoryName the category name (may be {@code null})
     * @param start the slot start time (may be {@code null})
     * @return key in the form {@code "<normalizedCategory>|<startIsoString>"} (start part may be empty)
     */
    private static String slotKey(String categoryName, java.time.LocalDateTime start) {
        return norm(categoryName) + "|" + (start != null ? start.toString() : "");
    }

    /**
     * Reads the internal cancellation-usage map from {@link DataRepository} using reflection.
     *
     * <p>If the field cannot be read or is not a {@link Map}, an empty map is returned.</p>
     *
     * @param repo repository to inspect
     * @return a defensive copy of the cancellation-usage map
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
     * Writes the internal cancellation-usage map into {@link DataRepository} using reflection.
     *
     * @param repo repository to modify
     * @param map map to set
     */
    private static void writeCancelMap(DataRepository repo, Map<String, Boolean> map) {
        try {
            Field f = DataRepository.class.getDeclaredField("cancelUsedByUserCategory");
            f.setAccessible(true);
            f.set(repo, map);
        } catch (Exception ignored) {}
    }

    /**
     * Sets a static integer counter field (e.g., {@code counter}) on a domain class using reflection.
     *
     * @param cls the class that declares the static counter field
     * @param field the field name
     * @param nextValue the next value that should be assigned
     */
    private static void setStaticCounter(Class<?> cls, String field, int nextValue) {
        try {
            Field f = cls.getDeclaredField(field);
            f.setAccessible(true);
            f.setInt(null, nextValue);
        } catch (Exception ignored) {}
    }

    /**
     * Forces the internal state of an {@link Appointment} to match the snapshot DTO, using reflection.
     *
     * @param a appointment to mutate
     * @param dto snapshot DTO containing the desired state
     */
    private static void forceAppointmentState(Appointment a, AppointmentDTO dto) {
        try {
            setField(a, "status", AppointmentStatus.valueOf(dto.status));
            setField(a, "confirmedAt", dto.confirmedAt);
            setField(a, "cancelledAt", dto.cancelledAt);
        } catch (Exception ignored) {}
    }

    /**
     * Forces the internal state of a {@link BookingRequest} to match the snapshot DTO, using reflection.
     *
     * @param r booking request to mutate
     * @param dto snapshot DTO containing the desired state
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
     * Forces the internal state of a {@link ContactRequest} to match the snapshot DTO, using reflection.
     *
     * @param r contact request to mutate
     * @param dto snapshot DTO containing the desired state
     */
    private static void forceContactRequestState(ContactRequest r, ContactRequestDTO dto) {
        try {
            setField(r, "read", dto.read);
        } catch (Exception ignored) {}
    }

    /**
     * Forces the internal state of an {@link AuditEvent} to match the snapshot DTO.
     *
     * <p>Currently does not set any extra fields beyond what is set in the constructor.</p>
     *
     * @param e audit event to mutate
     * @param dto snapshot DTO containing the desired state
     */
    private static void forceAuditEventState(AuditEvent e, AuditEventDTO dto) {
        try {
        } catch (Exception ignored) {}
    }

    /**
     * Sets a declared field value on the given object using reflection.
     *
     * @param obj object to modify
     * @param name declared field name on {@code obj.getClass()}
     * @param value value to assign
     * @throws Exception if the field cannot be found or set
     */
    private static void setField(Object obj, String name, Object value) throws Exception {
        Field f = obj.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(obj, value);
    }
}