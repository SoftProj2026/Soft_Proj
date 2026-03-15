package Service;

import domain.AppointmentType;

/**
 * Centralized business rules and validation for each appointment type (US5.2).
 * Returns "OK" if all data is valid, otherwise provides an error message.
 *
 * @author remaa
 * @version 1.0
 */
public final class AppointmentTypeRules {

    /**
     * Private constructor to prevent instantiation.
     */
    private AppointmentTypeRules() {}

    /**
     * Validates booking rules for the selected appointment type and its parameters.
     *
     * @param type        the appointment type
     * @param duration    the appointment duration in minutes
     * @param participants number of participants
     * @param groupSize   the group size (if type is GROUP), else null
     * @return "OK" if valid, otherwise error message
     */
    public static String validate(AppointmentType type, int duration, int participants, Integer groupSize) {
        if (type == null) return "Please select appointment type.";

        if (duration < 1) return "Duration must be at least 1 minute.";

        if (participants < 1 || participants > 5) return "Participants must be between 1 and 5.";

        switch (type) {
            case INDIVIDUAL:
                if (participants != 1) return "Individual appointment must have exactly 1 participant.";
                if (groupSize != null) return "Group size is not applicable for Individual.";
                return "OK";

            case GROUP:
                if (groupSize == null) return "Group size is required for Group appointments.";
                if (groupSize < 1 || groupSize > 5) return "Group size must be between 1 and 5.";
                if (participants < 2) return "Group appointment must have at least 2 participants.";
                return "OK";

            case REVIEW:
                if (groupSize != null) return "Group size is only applicable for Group appointments.";
                return "OK";

            case EMERGENCY:
                if (groupSize != null) return "Group size is only applicable for Group appointments.";
                return "OK";

            case NEW_APPOINTMENT:
            default:
                if (groupSize != null) return "Group size is only applicable for Group appointments.";
                return "OK";
        }
    }
}