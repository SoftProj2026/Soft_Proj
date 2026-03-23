package Test;

import Service.ParticipantLimitRule;
import domain.Appointment;
import domain.TimeSlot;
import domain.User;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ParticipantLimitRule
 */
public class ParticipantLimitRuleTest {

    /**
     * Helper method to create a sample appointment
     */
    private Appointment createAppointment(int participants) {

        User user = new User(
                "Test",
                "User",
                "testuser",
                "pw",
                LocalDate.of(1990, 1, 1),
                "t@x.com"
        );

        TimeSlot slot =
                new TimeSlot(
                        LocalDateTime.now().plusDays(1),
                        30
                );

        return new Appointment(
                user,
                slot,
                30,
                participants
        );
    }

    @Test
    void isValid_returns_true_when_participants_less_than_limit() {

        ParticipantLimitRule rule =
                new ParticipantLimitRule(5);

        Appointment appointment =
                createAppointment(3);

        assertTrue(
                rule.isValid(appointment)
        );
    }

    @Test
    void isValid_returns_true_when_participants_equal_to_limit() {

        ParticipantLimitRule rule =
                new ParticipantLimitRule(5);

        Appointment appointment =
                createAppointment(5);

        assertTrue(
                rule.isValid(appointment)
        );
    }

    @Test
    void isValid_returns_false_when_participants_exceed_limit() {

        ParticipantLimitRule rule =
                new ParticipantLimitRule(5);

        Appointment appointment =
                createAppointment(6);

        assertFalse(
                rule.isValid(appointment)
        );
    }

    @Test
    void getErrorMessage_returns_correct_message() {

        ParticipantLimitRule rule =
                new ParticipantLimitRule(5);

        assertEquals(
                "Participant limit exceeded.",
                rule.getErrorMessage()
        );
    }

    @Test
    void different_limits_are_handled_correctly() {

        ParticipantLimitRule smallRule =
                new ParticipantLimitRule(2);

        ParticipantLimitRule largeRule =
                new ParticipantLimitRule(10);

        Appointment appointment =
                createAppointment(3);

        assertFalse(
                smallRule.isValid(appointment)
        );

        assertTrue(
                largeRule.isValid(appointment)
        );
    }
}