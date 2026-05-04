package service;

import domain.Appointment;
import domain.AppointmentStatus;
import domain.Category;
import domain.TimeSlot;
import domain.User;
import persistence.DataRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SmartSlotSuggestionService {

    private final DataRepository repo;
    private final BlockedSlotsRule blockedRule = new BlockedSlotsRule();

    public SmartSlotSuggestionService(DataRepository repo) {
        this.repo = repo;
    }

    public List<TimeSlot> suggest(User user, Category category, int limit) {
        if (user == null || category == null) return List.of();
        LocalDateTime now = LocalDateTime.now();

        List<TimeSlot> candidates = new ArrayList<>();
        for (TimeSlot slot : repo.getSlots()) {
            if (slot == null || slot.getStartDateTime() == null || slot.getEndDateTime() == null) continue;
            if (slot.getCategory() == null || slot.getCategory().getName() == null) continue;

            if (!slot.getCategory().getName().equalsIgnoreCase(category.getName())) continue;

            if (!slot.getStartDateTime().isAfter(now)) continue;              // future only
            if (!slot.isAvailable()) continue;                                 // company available
            if (blockedRule.getBlockMessageIfBlocked(slot) != null) continue;  // not break blocked
            if (isUserBusy(user, slot)) continue;                              // user free

            candidates.add(slot);
        }

        // Ranking: closer time is better
        candidates.sort(Comparator.comparingLong(s ->
                Math.abs(Duration.between(now, s.getStartDateTime()).toMinutes())
        ));

        if (limit <= 0) limit = 5;
        return candidates.size() <= limit ? candidates : candidates.subList(0, limit);
    }

    private boolean isUserBusy(User user, TimeSlot slot) {
        String username = user.getUsername();

        for (Appointment a : repo.getAppointments()) {
            if (a == null) continue;
            if (a.getStatus() != AppointmentStatus.CONFIRMED) continue;
            if (a.getUser() == null || a.getUser().getUsername() == null) continue;
            if (!a.getUser().getUsername().equalsIgnoreCase(username)) continue;
            if (a.getSlot() == null) continue;

            TimeSlot existing = a.getSlot();
            boolean overlap =
                    slot.getStartDateTime().isBefore(existing.getEndDateTime()) &&
                    slot.getEndDateTime().isAfter(existing.getStartDateTime());

            if (overlap) return true;
        }
        return false;
    }
}