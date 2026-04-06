package Service;

import domain.Category;
import domain.TimeSlot;
import domain.User;
import persistence.DataRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;



public class AiBookingAssistantService {

    private final DataRepository repo;
    private final BookingRequestService requestService;
    private final SmartSlotSuggestionService suggester;

    public AiBookingAssistantService(DataRepository repo) {
        this.repo = Objects.requireNonNull(repo);
        this.requestService = new BookingRequestService(repo);
        this.suggester = new SmartSlotSuggestionService(repo);
    }

    
    public List<TimeSlot> suggestTopMutualSlots(User user, Category category, int limit) {
        if (user == null || category == null) return Collections.emptyList();
        if (limit <= 0) limit = 1;

        List<TimeSlot> raw = suggester.suggest(user, category, limit);

        List<TimeSlot> choices = new ArrayList<>(raw);
        choices.removeIf(s -> s == null
                || s.getStartDateTime() == null
                || !s.getStartDateTime().isAfter(LocalDateTime.now()));

        return choices;
    }

    
    public BookingResult sendRequestForSlot(User user, TimeSlot selectedSlot, int duration, int participants) {
        if (user == null) return new BookingResult(false, "Invalid user.");
        if (selectedSlot == null) return new BookingResult(false, "Please select a slot.");

        LocalDateTime start = selectedSlot.getStartDateTime();
        if (start == null || !start.isAfter(LocalDateTime.now())) {
            return new BookingResult(false, "Selected slot is invalid/past.");
        }

        return requestService.submitRequest(user, selectedSlot, duration, participants);
    }

    
    public BookingResult aiPickAndSendRequest(User user, Category category, int duration, int participants) {
        if (user == null) return new BookingResult(false, "Invalid user.");
        if (category == null) return new BookingResult(false, "Please select a category.");

        List<TimeSlot> choices = suggester.suggest(user, category, 1);
        if (choices.isEmpty()) {
            return new BookingResult(false, "AI could not find any suitable mutual slot for this category.");
        }

        TimeSlot chosen = choices.get(0);

        LocalDateTime start = chosen.getStartDateTime();
        if (start == null || !start.isAfter(LocalDateTime.now())) {
            return new BookingResult(false, "AI selected an invalid/past slot. Please try again.");
        }

        return requestService.submitRequest(user, chosen, duration, participants);
    }
}