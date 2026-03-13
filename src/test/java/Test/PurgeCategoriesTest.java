package Test;


import domain.Category;
import domain.TimeSlot;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import persistence.DataRepository;
public class PurgeCategoriesTest {

    @Test
    void purgeCategories_removesCategory_andRelatedSlots() {
        DataRepository repo = new DataRepository();

        Category lab = new Category("Lab Reservation");
        repo.addCategory(lab);

        repo.addSlot(new TimeSlot(LocalDateTime.now().plusDays(1), 60, lab));

        int removed = repo.purgeCategories(Set.of("Lab Reservation"));

        assertEquals(1, removed);
        assertTrue(repo.getCategories().stream().noneMatch(c -> c.getName().equalsIgnoreCase("Lab Reservation")));
        assertTrue(repo.getSlots().stream().noneMatch(s -> s.getCategory().getName().equalsIgnoreCase("Lab Reservation")));
    }

    @Test
    void purgeCategories_isCaseInsensitive() {
        DataRepository repo = new DataRepository();

        Category c = new Category("Exam Hall");
        repo.addCategory(c);

        int removed = repo.purgeCategories(Set.of("eXaM hAlL"));

        assertEquals(1, removed);
        assertTrue(repo.getCategories().stream().noneMatch(x -> x.getName().equalsIgnoreCase("Exam Hall")));
    }
}