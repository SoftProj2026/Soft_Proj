package Service;


 // Represents result of booking attempt
 
public class BookingResult {

    private boolean success;
    private String message;

    public BookingResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}