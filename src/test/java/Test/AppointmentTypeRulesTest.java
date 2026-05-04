package Test;

import domain.AppointmentType;
import service.AppointmentTypeRules;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AppointmentTypeRulesTest {

    @Test
    void nullType_returnsError() {
        String r = AppointmentTypeRules.validate(null, 10, 1, null);
        assertEquals("Please select appointment type.", r);
    }

    @Test
    void invalidDurationAndParticipants() {
        assertEquals("Duration must be at least 1 minute.",
                AppointmentTypeRules.validate(AppointmentType.NEW_APPOINTMENT, 0, 1, null));

        assertEquals("Participants must be between 1 and 5.",
                AppointmentTypeRules.validate(AppointmentType.NEW_APPOINTMENT, 10, 0, null));
    }

    @Test
    void individual_enforcesSingleParticipant_and_noGroupSize() {
        assertEquals("Individual appointment must have exactly 1 participant.",
                AppointmentTypeRules.validate(AppointmentType.INDIVIDUAL, 15, 2, null));

        assertEquals("Group size is not applicable for Individual.",
                AppointmentTypeRules.validate(AppointmentType.INDIVIDUAL, 15, 1, 2));

        assertEquals("OK",
                AppointmentTypeRules.validate(AppointmentType.INDIVIDUAL, 15, 1, null));
    }

    @Test
    void group_requiresGroupSize_and_minParticipants() {
        assertEquals("Group size is required for Group appointments.",
                AppointmentTypeRules.validate(AppointmentType.GROUP, 30, 3, null));

        assertEquals("Group size must be between 1 and 5.",
                AppointmentTypeRules.validate(AppointmentType.GROUP, 30, 3, 0));

        assertEquals("Group appointment must have at least 2 participants.",
                AppointmentTypeRules.validate(AppointmentType.GROUP, 30, 1, 2));

        assertEquals("OK", AppointmentTypeRules.validate(AppointmentType.GROUP, 30, 3, 3));
    }

    @Test
    void review_emergency_new_doNotAllowGroupSize() {
        assertEquals("Group size is only applicable for Group appointments.",
                AppointmentTypeRules.validate(AppointmentType.REVIEW, 10, 1, 2));
        assertEquals("Group size is only applicable for Group appointments.",
                AppointmentTypeRules.validate(AppointmentType.EMERGENCY, 10, 1, 2));
        assertEquals("Group size is only applicable for Group appointments.",
                AppointmentTypeRules.validate(AppointmentType.NEW_APPOINTMENT, 10, 1, 2));

        assertEquals("OK", AppointmentTypeRules.validate(AppointmentType.REVIEW, 10, 1, null));
        assertEquals("OK", AppointmentTypeRules.validate(AppointmentType.EMERGENCY, 10, 1, null));
    }
}