package Service;

import domain.Appointment;
import persistence.DataRepository;

import java.util.ArrayList;
import java.util.List;

public class BookingService {

    private DataRepository repo;
    private List<BookingRuleStrategy> rules = new ArrayList<>();

    public BookingService(DataRepository repo) {
        this.repo = repo;

        rules.add(new DurationRule(60));
        rules.add(new ParticipantLimitRule(5));
        rules.add(new OverlapRule(repo)); // منع التعارض
    }

    public BookingResult book(Appointment appointment) {

        for (BookingRuleStrategy rule : rules) {
            if (!rule.isValid(appointment)) {
                return new BookingResult(false,
                        rule.getErrorMessage());
            }
        }

        appointment.confirm();
        repo.addAppointment(appointment);

        return new BookingResult(true,
                "Appointment booked successfully.");
    }
}