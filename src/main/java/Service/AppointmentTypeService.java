package Service;

import domain.Appointment;
import domain.AppointmentStatus;
import domain.AppointmentType;
import domain.User;
import persistence.DataRepository;
import persistence.RepoStorage;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Service for updating the type of an appointment, applying business rules,
 * and sending notifications for special types like EMERGENCY.
 *
 * @author Qussaialaw
 * @version 1.0
 */
public class AppointmentTypeService {

    /**
     * Emergency contact phone for company use in email notification.
     */
    public static final String COMPANY_EMERGENCY_PHONE = "059-507-9549";

    /**
     * The data repository reference.
     */
    private final DataRepository repo;

    /**
     * Email sender for sending emergency emails.
     */
    private final EmailSender emailSender;

    /**
     * Constructs an AppointmentTypeService.
     *
     * @param repo        the data repository, must not be null
     * @param emailSender email sender implementation, must not be null
     */
    public AppointmentTypeService(DataRepository repo, EmailSender emailSender) {
        this.repo = Objects.requireNonNull(repo);
        this.emailSender = Objects.requireNonNull(emailSender);
    }

    /**
     * Sets the type and additional properties of a confirmed appointment.
     * If the type is EMERGENCY this method will attempt to send an emergency
     * notification email to the appointment's user and a copy to the company.
     *
     * @param appointment                  the appointment to update; must not be null
     * @param type                         the {@link AppointmentType} to assign; must not be null
     * @param groupSize                    group size for GROUP type, or null
     * @param reviewTargetSlotStart        review slot start time for REVIEW type, or null
     * @param emergencyPreferredSlotStart  preferred slot start time for EMERGENCY type, or null
     * @return "Saved." on success or an error message describing why the operation failed
     */
    public String setAppointmentType(Appointment appointment,
                                     AppointmentType type,
                                     Integer groupSize,
                                     LocalDateTime reviewTargetSlotStart,
                                     LocalDateTime emergencyPreferredSlotStart) {

        if (appointment == null) return "Invalid appointment.";
        if (type == null) return "Please select a type.";

        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            return "Only CONFIRMED appointments can be classified.";
        }

        int duration = appointment.getDurationInMinutes();
        int participants = appointment.getParticipants();
        String rulesMsg = AppointmentTypeRules.validate(type, duration, participants, groupSize);
        if (!"OK".equals(rulesMsg)) return rulesMsg;

        if (type == AppointmentType.REVIEW) {
            if (reviewTargetSlotStart == null) return "Please select an available slot for Review.";
        }

        if (type == AppointmentType.EMERGENCY) {
            if (emergencyPreferredSlotStart == null) return "Please select an available slot time for Emergency.";
        }

        appointment.setAppointmentType(type);

        if (type == AppointmentType.GROUP) {
            appointment.setGroupSize(groupSize);
        }

        if (type == AppointmentType.REVIEW) {
            appointment.setReviewTargetSlotStart(reviewTargetSlotStart);
        }

        if (type == AppointmentType.EMERGENCY) {
            appointment.setEmergencyPreferredSlotStart(emergencyPreferredSlotStart);

            User u = appointment.getUser();
            String email = (u != null) ? u.getEmail() : null;

            if (email == null || email.trim().isEmpty()) {
                RepoStorage.save(repo);
                return "Emergency selected, but user email is missing.";
            }

            String subject = "Emergency Appointment - Ref #" + appointment.getId();
            String body = buildEmergencyEmailBody(appointment);

            emailSender.send("noreply@qrbooking.local", email.trim(), subject, body);
        }

        RepoStorage.save(repo);
        return "Saved.";
    }

    /**
     * Builds the message body for emergency appointment email.
     *
     * @param appointment appointment marked as emergency; may not be null
     * @return the email body text including reference, category, preferred time and company phone
     */
    private String buildEmergencyEmailBody(Appointment appointment) {
        String cat = (appointment.getSlot() != null && appointment.getSlot().getCategory() != null)
                ? appointment.getSlot().getCategory().getName()
                : "N/A";

        String selected = (appointment.getEmergencyPreferredSlotStart() != null)
                ? appointment.getEmergencyPreferredSlotStart().toString()
                : "N/A";

        return "Your appointment has been marked as EMERGENCY.\n"
                + "Reference: #" + appointment.getId() + "\n"
                + "Category: " + cat + "\n"
                + "Preferred emergency time: " + selected + "\n"
                + "Company contact phone: " + COMPANY_EMERGENCY_PHONE + "\n";
    }
}