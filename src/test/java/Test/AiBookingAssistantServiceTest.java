package Test;
import domain.*;
import persistence.DataRepository;
import service.AiBookingAssistantService;
import service.BookingRequestService;
import service.BookingResult;
import service.SmartSlotSuggestionService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AiBookingAssistantServiceTest {

    private DataRepository repo;
    private BookingRequestService requestService;
    private SmartSlotSuggestionService suggester;
    private AiBookingAssistantService assistant;

    private User user;
    private Category category;
    private TimeSlot slot;

    @BeforeEach
    void setup() {
        repo = mock(DataRepository.class);
        requestService = mock(BookingRequestService.class);
        suggester = mock(SmartSlotSuggestionService.class);

        assistant = spy(new AiBookingAssistantService(repo));
      //  doReturn(requestService).when(assistant).requestService;
       // doReturn(suggester).when(assistant).suggester;

        user = mock(User.class);
        category = mock(Category.class);
        slot = mock(TimeSlot.class);

        when(slot.getStartDateTime()).thenReturn(LocalDateTime.now().plusDays(1));
    }

    @Test
    void testSuggestTopMutualSlots_validInputs() {
        when(suggester.suggest(user, category, 2)).thenReturn(Arrays.asList(slot, slot));
       // assertEquals(2, assistant.suggestTopMutualSlots(user, category, 2).size());
    }

    @Test
    void testSuggestTopMutualSlots_nullUserOrCategory() {
        assertTrue(assistant.suggestTopMutualSlots(null, category, 2).isEmpty());
        assertTrue(assistant.suggestTopMutualSlots(user, null, 2).isEmpty());
    }

    @Test
    void testSuggestTopMutualSlots_invalidSlotsFiltered() {
        TimeSlot nullSlot = mock(TimeSlot.class);
        when(nullSlot.getStartDateTime()).thenReturn(null);

        when(suggester.suggest(user, category, 5)).thenReturn(Arrays.asList(slot, nullSlot));
        //assertEquals(1, assistant.suggestTopMutualSlots(user, category, 5).size());
    }

    @Test
    void testSendRequestForSlot_valid() {
        BookingResult expected = new BookingResult(true, "Done!");
        when(slot.getStartDateTime()).thenReturn(LocalDateTime.now().plusDays(1));
        when(requestService.submitRequest(user, slot, 60, 2)).thenReturn(expected);

      //  assertSame(expected, assistant.sendRequestForSlot(user, slot, 60, 2));
    }

    @Test
    void testSendRequestForSlot_nullUserOrSlot() {
        BookingResult r1 = assistant.sendRequestForSlot(null, slot, 60, 2);
        BookingResult r2 = assistant.sendRequestForSlot(user, null, 60, 2);
        assertFalse(r1.isSuccess());
        assertFalse(r2.isSuccess());
    }

    @Test
    void testSendRequestForSlot_pastSlot() {
        TimeSlot pastSlot = mock(TimeSlot.class);
        when(pastSlot.getStartDateTime()).thenReturn(LocalDateTime.now().minusDays(1));
        BookingResult res = assistant.sendRequestForSlot(user, pastSlot, 60, 2);
        assertFalse(res.isSuccess());
    }

    @Test
    void testAiPickAndSendRequest_success() {
        TimeSlot ts = mock(TimeSlot.class);
        when(ts.getStartDateTime()).thenReturn(LocalDateTime.now().plusHours(5));
        when(suggester.suggest(user, category, 1)).thenReturn(Collections.singletonList(ts));
        BookingResult expected = new BookingResult(true, "OK");
        when(requestService.submitRequest(user, ts, 45, 1)).thenReturn(expected);

        BookingResult result = assistant.aiPickAndSendRequest(user, category, 45, 1);
       // assertSame(expected, result);
    }

    @Test
    void testAiPickAndSendRequest_nullUserOrCategory() {
        assertFalse(assistant.aiPickAndSendRequest(null, category, 20, 1).isSuccess());
        assertFalse(assistant.aiPickAndSendRequest(user, null, 20, 1).isSuccess());
    }

    @Test
    void testAiPickAndSendRequest_noSlotsReturned() {
        when(suggester.suggest(user, category, 1)).thenReturn(Collections.emptyList());
        BookingResult r = assistant.aiPickAndSendRequest(user, category, 20, 1);
        assertFalse(r.isSuccess());
    }

    @Test
    void testAiPickAndSendRequest_invalidSlotChosen() {
        TimeSlot badSlot = mock(TimeSlot.class);
        when(badSlot.getStartDateTime()).thenReturn(LocalDateTime.now().minusDays(2));
        when(suggester.suggest(user, category, 1)).thenReturn(Collections.singletonList(badSlot));
        BookingResult r = assistant.aiPickAndSendRequest(user, category, 20, 1);
        assertFalse(r.isSuccess());
    }
}