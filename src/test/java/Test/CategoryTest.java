package Test;

import domain.Category;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CategoryTest {
    @Test
    void category_toString_returns_name() {
        Category c = new Category("TestCat");
        assertEquals("TestCat", c.toString());
        assertNull(c.getCategory()); 
    }
}