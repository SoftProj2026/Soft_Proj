package service;

import domain.Appointment;
import domain.AppointmentStatus;
import domain.AppointmentType;
import domain.TimeSlot;
import domain.User;
import persistence.DataRepository;
import persistence.RepoStorage;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Service to create a NEW confirmed additional appointment for a user in a new slot.
 * Does not modify the existing appointment.
 *
 * @author remaa
 * @version 1.0
 */
public class AdditionalAppointmentService {

    /**
     * The data repository where appointments are stored.
     */
    private final DataRepository repo;

    /**
     * Constructs an AdditionalAppointmentService.
     *
     * @param repo the data repository (must not be null)
     */
    public AdditionalAppointmentService(DataRepository repo) {
        this.repo = Objects.requireNonNull(repo);
    }

    /**
     * Creates and stores a new appointment for the given user and slot, confirming it immediately. 
     * Applies appointment type rules (US5.2).
     *
     * @param user the user creating the appointment
     * @param newSlot the selected available timeslot
     * @param durationMinutes the appointment duration in minutes
     * @param participants the number of participants (1–5)
     * @param type the selected appointment type
     * @param groupSize the group size if GROUP appointment type, else null
     * @param emergencyPreferredSlotStart preferred slot for emergency, if any
     * @return "Saved." if successful, otherwise an error message
     */
    public String createNewAppointment(User user,
                                       TimeSlot newSlot,
                                       int durationMinutes,
                                       int participants,
                                       AppointmentType type,
                                       Integer groupSize,
                                       LocalDateTime emergencyPreferredSlotStart) {

        if (user == null) return "Invalid user.";
        if (newSlot == null) return "Please select an available slot.";
        if (newSlot.getStartDateTime() == null || newSlot.getEndDateTime() == null) return "Invalid slot time.";

        if (!newSlot.getStartDateTime().isAfter(LocalDateTime.now())) {
            return "You cannot book a past time slot.";
        }

        if (!newSlot.isAvailable()) {
            return "Selected time slot is not available.";
        }

        if (participants < 1 || participants > 5) return "Participants must be between 1 and 5.";
        if (durationMinutes < 1) return "Duration must be at least 1 minute.";

        int slotMinutes = (int) Duration.between(newSlot.getStartDateTime(), newSlot.getEndDateTime()).toMinutes();
        if (durationMinutes > slotMinutes) {
            return "Invalid duration. Max allowed for this slot is " + slotMinutes + " minutes.";
        }

        String rules = AppointmentTypeRules.validate(type, durationMinutes, participants, groupSize);
        if (!"OK".equals(rules)) return rules;

        Appointment a = new Appointment(user, newSlot, durationMinutes, participants);
        a.confirm();
        a.setAppointmentType(type);

        if (type == AppointmentType.GROUP) {
            a.setGroupSize(groupSize);
        }

        if (type == AppointmentType.EMERGENCY) {
            a.setEmergencyPreferredSlotStart(emergencyPreferredSlotStart);
        }

        repo.addAppointment(a);
        RepoStorage.save(repo);

        return "Saved.";
    }
}