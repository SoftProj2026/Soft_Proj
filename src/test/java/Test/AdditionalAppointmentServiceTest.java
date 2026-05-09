package Test;

import domain.Appointment;
import domain.AppointmentStatus;
import domain.AppointmentType;
import domain.Category;
import domain.TimeSlot;
import domain.User;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import persistence.DataRepository;
import persistence.RepoStorage;
import service.AdditionalAppointmentService;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AdditionalAppointmentServiceTest {

    private final Category category = new Category("C");

    private User user() {
        return new User(
                "First",
                "Last",
                "bob",
                "pw",
                null,
                "bob@example.com"
        );
    }

    private TimeSlot futureSlot(int durationMinutes) {
        return new TimeSlot(
                LocalDateTime.now().plusDays(1),
                durationMinutes,
                category
        );
    }

    @Test
    void constructor_nullRepo_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () ->
                new AdditionalAppointmentService(null)
        );
    }

    @Test
    void createNewAppointment_rejectsNullUser() {
        DataRepository repo = new DataRepository();
        AdditionalAppointmentService svc = new AdditionalAppointmentService(repo);

        String result = svc.createNewAppointment(
                null,
                futureSlot(60),
                30,
                1,
                AppointmentType.NEW_APPOINTMENT,
                null,
                null
        );

        assertEquals("Invalid user.", result);
        assertTrue(repo.getAppointments().isEmpty());
    }

    @Test
    void createNewAppointment_rejectsNullSlot() {
        DataRepository repo = new DataRepository();
        AdditionalAppointmentService svc = new AdditionalAppointmentService(repo);

        String result = svc.createNewAppointment(
                user(),
                null,
                30,
                1,
                AppointmentType.NEW_APPOINTMENT,
                null,
                null
        );

        assertEquals("Please select an available slot.", result);
        assertTrue(repo.getAppointments().isEmpty());
    }

    @Test
    void createNewAppointment_rejectsInvalidSlotStartTime() {
        DataRepository repo = new DataRepository();
        AdditionalAppointmentService svc = new AdditionalAppointmentService(repo);

        TimeSlot slot = mock(TimeSlot.class);
        when(slot.getStartDateTime()).thenReturn(null);

        String result = svc.createNewAppointment(
                user(),
                slot,
                30,
                1,
                AppointmentType.NEW_APPOINTMENT,
                null,
                null
        );

        assertEquals("Invalid slot time.", result);
        assertTrue(repo.getAppointments().isEmpty());
    }

    @Test
    void createNewAppointment_rejectsInvalidSlotEndTime() {
        DataRepository repo = new DataRepository();
        AdditionalAppointmentService svc = new AdditionalAppointmentService(repo);

        TimeSlot slot = mock(TimeSlot.class);
        when(slot.getStartDateTime()).thenReturn(LocalDateTime.now().plusDays(1));
        when(slot.getEndDateTime()).thenReturn(null);

        String result = svc.createNewAppointment(
                user(),
                slot,
                30,
                1,
                AppointmentType.NEW_APPOINTMENT,
                null,
                null
        );

        assertEquals("Invalid slot time.", result);
        assertTrue(repo.getAppointments().isEmpty());
    }

    @Test
    void createNewAppointment_rejectsPastSlot() {
        DataRepository repo = new DataRepository();
        AdditionalAppointmentService svc = new AdditionalAppointmentService(repo);

        TimeSlot past = new TimeSlot(
                LocalDateTime.now().minusHours(2),
                60,
                category
        );

        String result = svc.createNewAppointment(
                user(),
                past,
                30,
                1,
                AppointmentType.NEW_APPOINTMENT,
                null,
                null
        );

        assertEquals("You cannot book a past time slot.", result);
        assertTrue(repo.getAppointments().isEmpty());
    }

    @Test
    void createNewAppointment_rejectsUnavailableSlot() {
        DataRepository repo = new DataRepository();
        AdditionalAppointmentService svc = new AdditionalAppointmentService(repo);

        TimeSlot slot = futureSlot(60);
        slot.book();

        String result = svc.createNewAppointment(
                user(),
                slot,
                30,
                1,
                AppointmentType.NEW_APPOINTMENT,
                null,
                null
        );

        assertEquals("Selected time slot is not available.", result);
        assertTrue(repo.getAppointments().isEmpty());
    }

    @Test
    void createNewAppointment_rejectsParticipantsLessThanOne() {
        DataRepository repo = new DataRepository();
        AdditionalAppointmentService svc = new AdditionalAppointmentService(repo);

        String result = svc.createNewAppointment(
                user(),
                futureSlot(60),
                30,
                0,
                AppointmentType.NEW_APPOINTMENT,
                null,
                null
        );

        assertEquals("Participants must be between 1 and 5.", result);
        assertTrue(repo.getAppointments().isEmpty());
    }

    @Test
    void createNewAppointment_rejectsParticipantsGreaterThanFive() {
        DataRepository repo = new DataRepository();
        AdditionalAppointmentService svc = new AdditionalAppointmentService(repo);

        String result = svc.createNewAppointment(
                user(),
                futureSlot(60),
                30,
                6,
                AppointmentType.NEW_APPOINTMENT,
                null,
                null
        );

        assertEquals("Participants must be between 1 and 5.", result);
        assertTrue(repo.getAppointments().isEmpty());
    }

    @Test
    void createNewAppointment_rejectsDurationLessThanOne() {
        DataRepository repo = new DataRepository();
        AdditionalAppointmentService svc = new AdditionalAppointmentService(repo);

        String result = svc.createNewAppointment(
                user(),
                futureSlot(60),
                0,
                1,
                AppointmentType.NEW_APPOINTMENT,
                null,
                null
        );

        assertEquals("Duration must be at least 1 minute.", result);
        assertTrue(repo.getAppointments().isEmpty());
    }

    @Test
    void createNewAppointment_rejectsDurationLongerThanSlot() {
        DataRepository repo = new DataRepository();
        AdditionalAppointmentService svc = new AdditionalAppointmentService(repo);

        String result = svc.createNewAppointment(
                user(),
                futureSlot(60),
                120,
                1,
                AppointmentType.NEW_APPOINTMENT,
                null,
                null
        );

        assertEquals("Invalid duration. Max allowed for this slot is 60 minutes.", result);
        assertTrue(repo.getAppointments().isEmpty());
    }

    @Test
    void createNewAppointment_rejectsNullAppointmentTypeFromRules() {
        DataRepository repo = new DataRepository();
        AdditionalAppointmentService svc = new AdditionalAppointmentService(repo);

        String result = svc.createNewAppointment(
                user(),
                futureSlot(60),
                30,
                1,
                null,
                null,
                null
        );

        assertEquals("Please select appointment type.", result);
        assertTrue(repo.getAppointments().isEmpty());
    }

    @Test
    void createNewAppointment_rejectsInvalidGroupRules() {
        DataRepository repo = new DataRepository();
        AdditionalAppointmentService svc = new AdditionalAppointmentService(repo);

        String missingGroupSize = svc.createNewAppointment(
                user(),
                futureSlot(60),
                30,
                3,
                AppointmentType.GROUP,
                null,
                null
        );

        assertEquals("Group size is required for Group appointments.", missingGroupSize);

        String oneParticipant = svc.createNewAppointment(
                user(),
                futureSlot(60),
                30,
                1,
                AppointmentType.GROUP,
                2,
                null
        );

        assertEquals("Group appointment must have at least 2 participants.", oneParticipant);

        assertTrue(repo.getAppointments().isEmpty());
    }

    @Test
    void createNewAppointment_rejectsInvalidIndividualRules() {
        DataRepository repo = new DataRepository();
        AdditionalAppointmentService svc = new AdditionalAppointmentService(repo);

        String result = svc.createNewAppointment(
                user(),
                futureSlot(60),
                30,
                2,
                AppointmentType.INDIVIDUAL,
                null,
                null
        );

        assertEquals("Individual appointment must have exactly 1 participant.", result);
        assertTrue(repo.getAppointments().isEmpty());
    }

    @Test
    void createNewAppointment_newAppointmentSuccess_confirmsBooksSlotAddsAppointmentAndSaves() {
        DataRepository repo = new DataRepository();
        repo.addCategory(category);

        TimeSlot slot = futureSlot(60);
        repo.addSlot(slot);

        User u = user();
        repo.addUser(u);

        AdditionalAppointmentService svc = new AdditionalAppointmentService(repo);

        try (MockedStatic<RepoStorage> storage = mockStatic(RepoStorage.class)) {
            String result = svc.createNewAppointment(
                    u,
                    slot,
                    30,
                    1,
                    AppointmentType.NEW_APPOINTMENT,
                    null,
                    null
            );

            assertEquals("Saved.", result);
            assertEquals(1, repo.getAppointments().size());

            Appointment appointment = repo.getAppointments().get(0);

            assertEquals(u, appointment.getUser());
            assertEquals(slot, appointment.getSlot());
            assertEquals(30, appointment.getDurationInMinutes());
            assertEquals(1, appointment.getParticipants());
            assertEquals(AppointmentStatus.CONFIRMED, appointment.getStatus());
            assertEquals(AppointmentType.NEW_APPOINTMENT, appointment.getAppointmentType());
            assertFalse(slot.isAvailable(), "Slot should be booked after success");

            storage.verify(() -> RepoStorage.save(any(DataRepository.class)), times(1));
        }
    }

    @Test
    void createNewAppointment_groupSuccess_setsGroupSize() {
        DataRepository repo = new DataRepository();
        TimeSlot slot = futureSlot(60);
        User u = user();

        AdditionalAppointmentService svc = new AdditionalAppointmentService(repo);

        try (MockedStatic<RepoStorage> storage = mockStatic(RepoStorage.class)) {
            String result = svc.createNewAppointment(
                    u,
                    slot,
                    45,
                    3,
                    AppointmentType.GROUP,
                    3,
                    null
            );

            assertEquals("Saved.", result);
            assertEquals(1, repo.getAppointments().size());

            Appointment appointment = repo.getAppointments().get(0);

            assertEquals(AppointmentType.GROUP, appointment.getAppointmentType());
            assertEquals(Integer.valueOf(3), appointment.getGroupSize());
            assertEquals(AppointmentStatus.CONFIRMED, appointment.getStatus());
            assertFalse(slot.isAvailable());

            storage.verify(() -> RepoStorage.save(any(DataRepository.class)), times(1));
        }
    }

    @Test
    void createNewAppointment_emergencySuccess_setsEmergencyPreferredSlotStart() {
        DataRepository repo = new DataRepository();
        TimeSlot slot = futureSlot(60);
        User u = user();

        LocalDateTime preferred = slot.getStartDateTime().plusHours(2);

        AdditionalAppointmentService svc = new AdditionalAppointmentService(repo);

        try (MockedStatic<RepoStorage> storage = mockStatic(RepoStorage.class)) {
            String result = svc.createNewAppointment(
                    u,
                    slot,
                    30,
                    1,
                    AppointmentType.EMERGENCY,
                    null,
                    preferred
            );

            assertEquals("Saved.", result);
            assertEquals(1, repo.getAppointments().size());

            Appointment appointment = repo.getAppointments().get(0);

            assertEquals(AppointmentType.EMERGENCY, appointment.getAppointmentType());
            assertEquals(preferred, appointment.getEmergencyPreferredSlotStart());
            assertEquals(AppointmentStatus.CONFIRMED, appointment.getStatus());
            assertFalse(slot.isAvailable());

            storage.verify(() -> RepoStorage.save(any(DataRepository.class)), times(1));
        }
    }

    @Test
    void invalidInputs_doNotCallRepoStorageSave() {
        DataRepository repo = new DataRepository();
        AdditionalAppointmentService svc = new AdditionalAppointmentService(repo);

        try (MockedStatic<RepoStorage> storage = mockStatic(RepoStorage.class)) {
            String result = svc.createNewAppointment(
                    null,
                    null,
                    30,
                    1,
                    AppointmentType.NEW_APPOINTMENT,
                    null,
                    null
            );

            assertEquals("Invalid user.", result);

            storage.verify(() -> RepoStorage.save(any(DataRepository.class)), never());
        }
    }
}