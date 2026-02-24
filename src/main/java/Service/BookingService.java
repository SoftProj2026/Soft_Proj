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

        rules.add(new DurationRule(30));

        rules.add(new ParticipantLimitRule(3));

        rules.add(new SlotAvailabilityRule());
        rules.add(new UniqueStartTimeRule(repo));
        rules.add(new OverlapRule(repo));
        rules.add(new WorkingHoursRule());
        rules.add(new MinimumNoticeRule());
        rules.add(new DailyBookingLimitRule(repo));
        rules.add(new OneBookingPerCategoryRule(repo));
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