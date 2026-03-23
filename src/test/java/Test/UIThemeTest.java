package Test;

import org.junit.jupiter.api.Test;
import presentation.UITheme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for presentation.UITheme
 *
 * Tests run Swing operations on the EDT to avoid threading warnings.
 */
class UIThemeTest {

    private static void runOnEdt(Runnable r) throws Exception {
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            final Throwable[] err = new Throwable[1];
            SwingUtilities.invokeAndWait(() -> {
                try {
                    r.run();
                } catch (Throwable t) {
                    err[0] = t;
                }
            });
            if (err[0] != null) throw new RuntimeException(err[0]);
        }
    }

    @Test
    void color_constants_are_expected() {
        assertEquals(new Color(245, 248, 255), UITheme.BG, "BG color");
        assertEquals(Color.WHITE, UITheme.CARD, "CARD color");
        assertEquals(new Color(33, 120, 255), UITheme.PRIMARY, "PRIMARY color");
        assertEquals(new Color(18, 78, 180), UITheme.PRIMARY_DARK, "PRIMARY_DARK color");
        assertEquals(new Color(25, 35, 45), UITheme.TEXT, "TEXT color");
        assertEquals(new Color(110, 120, 135), UITheme.MUTED, "MUTED color");
    }

    @Test
    void apply_sets_ui_manager_defaults() throws Exception {
        runOnEdt(() -> UITheme.apply());

        Object defaultFont = UIManager.get("defaultFont");
        assertNotNull(defaultFont, "defaultFont must be set");
        assertTrue(defaultFont instanceof Font, "defaultFont must be a Font");

        Object panelBg = UIManager.get("Panel.background");
        Object viewportBg = UIManager.get("Viewport.background");
        assertNotNull(panelBg, "Panel.background present");
        assertEquals(UITheme.BG, panelBg, "Panel.background should equal UITheme.BG");
        assertNotNull(viewportBg, "Viewport.background present");
        assertEquals(UITheme.BG, viewportBg, "Viewport.background should equal UITheme.BG");

        Object tableFont = UIManager.get("Table.font");
        Object thFont = UIManager.get("TableHeader.font");
        assertNotNull(tableFont, "Table.font must be set");
        assertTrue(tableFont instanceof Font);
        assertNotNull(thFont, "TableHeader.font must be set");
        assertTrue(thFont instanceof Font);
    }

    @Test
    void primaryButton_has_expected_style() throws Exception {
        runOnEdt(() -> {
            JButton b = UITheme.primaryButton("ClickMe");
            assertEquals("ClickMe", b.getText());
            Font f = b.getFont();
            assertNotNull(f, "button font must not be null");
            assertTrue(f.isBold(), "button font should be bold");
            assertEquals(14, f.getSize(), "button font size should be 14");

            assertEquals(UITheme.PRIMARY_DARK, b.getBackground(), "primary button background");
            assertEquals(Color.WHITE, b.getForeground(), "primary button foreground");
            assertFalse(b.isFocusPainted(), "primary button focusPainted must be false");

            assertEquals(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR).getType(),
                         b.getCursor().getType(),
                         "primary button cursor type");

            assertNotNull(b.getBorder(), "primary button border must not be null");
            assertTrue(b.getBorder() instanceof EmptyBorder, "primary button border should be EmptyBorder");
        });
    }

    @Test
    void secondaryButton_has_expected_style() throws Exception {
        runOnEdt(() -> {
            JButton b = UITheme.secondaryButton("More");
            assertEquals("More", b.getText());
            Font f = b.getFont();
            assertNotNull(f);
            assertTrue(f.isBold());
            assertEquals(14, f.getSize());

            assertEquals(Color.WHITE, b.getBackground(), "secondary background should be white");
            assertEquals(UITheme.PRIMARY_DARK, b.getForeground(), "secondary foreground should be PRIMARY_DARK");

            assertEquals(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR).getType(),
                         b.getCursor().getType(),
                         "secondary button cursor type");

            assertNotNull(b.getBorder());
            assertTrue(b.getBorder() instanceof CompoundBorder, "secondary button border must be CompoundBorder");

            CompoundBorder cb = (CompoundBorder) b.getBorder();
            assertTrue(cb.getOutsideBorder() instanceof LineBorder, "outside border must be LineBorder");
            assertTrue(cb.getInsideBorder() instanceof EmptyBorder, "inside border must be EmptyBorder");

            LineBorder lb = (LineBorder) cb.getOutsideBorder();
            Color expectedLine = new Color(200, 215, 235);
            assertEquals(expectedLine.getRed(), lb.getLineColor().getRed());
            assertEquals(expectedLine.getGreen(), lb.getLineColor().getGreen());
            assertEquals(expectedLine.getBlue(), lb.getLineColor().getBlue());
            try {
                int thickness = (int) LineBorder.class.getMethod("getThickness").invoke(lb);
                assertEquals(2, thickness, "LineBorder thickness");
                boolean rounded = (boolean) LineBorder.class.getMethod("getRoundedCorners").invoke(lb);
                assertTrue(rounded, "LineBorder should be rounded corners");
            } catch (NoSuchMethodException ignored) {
            } catch (Exception ex) {
                fail("Unexpected reflection error checking LineBorder properties: " + ex.getMessage());
            }
        });
    }
}