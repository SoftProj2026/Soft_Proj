package Service;

import domain.Appointment;
import domain.AppointmentStatus;
import domain.TimeSlot;
import domain.User;
import persistence.DataRepository;

import java.time.Duration;
import java.time.LocalDateTime;

public class BookingEmailReminderService {

    private final DataRepository repo;
    private final EmailSender emailSender;
    // لا يوجد داعي لحقن companyEmail هنا، بل نأخذه من env عند الحاجة فقط

    public BookingEmailReminderService(DataRepository repo, EmailSender emailSender) {
        this.repo = repo;
        this.emailSender = emailSender;
    }

    public void send24hReminderIfNeeded(Appointment a) {
        if (a == null) return;
        if (a.getStatus() != AppointmentStatus.CONFIRMED) return;
        if (a.getUser() == null || a.getUser().getUsername() == null) return;

        User user = findUser(a.getUser().getUsername());
        if (user == null) return;

        String to = user.getEmail() != null ? user.getEmail().trim() : "";
        if (to.isEmpty()) {
            System.out.println("[BookingEmailReminderService] No user email found. Skipping.");
            return;
        }

        TimeSlot slot = a.getSlot();
        if (slot == null || slot.getStartDateTime() == null) return;

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = slot.getStartDateTime();
        if (!start.isAfter(now)) return;

        Duration until = Duration.between(now, start);

        // فقط أرسل إذا بقي أقل من 24 ساعة
        if (until.compareTo(Duration.ofHours(24)) >= 0) return;

        String from = getEnvCompanyEmail();
        if (from.isEmpty()) from = "noreply@example.com";

        String displayName = safeDisplayName(user);
        String cat = (slot.getCategory() != null) ? slot.getCategory().getName() : "N/A";

        String subject = "Final Reminder: Your booking deadline is in 24 hours\n"
                + "تنبيه أخير: بقي أقل من 24 ساعة على حجزك";
        String body =
                "This is a final reminder that your booking deadline is approaching; you have approximately 24 hours left to book.\n\n" +
                "هذه رسالة تذكير أخيرة بأن موعد الحجز الخاص بك يقترب، بقي حوالي 24 ساعة فقط.\n\n" +
                "Booking Details:\n" +
                "- Category: " + cat + "\n" +
                "- Start Time: " + start + "\n" +
                "- Appointment ID: #" + a.getId() + "\n" +
                "- User: " + displayName + " (" + to + ")\n\n" +
                "Please make sure to attend on time.\n" +
                "يرجى التأكد من الحضور في الوقت المحدد.\n\n" +
                "Thank you,\nQR Booking Team";

        emailSender.send(from, to, subject, body);
    }

    private User findUser(String username) {
        for (User u : repo.getUsers()) {
            if (u != null && u.getUsername() != null && u.getUsername().equalsIgnoreCase(username)) {
                return u;
            }
        }
        return null;
    }

    private static String safeDisplayName(User u) {
        if (u == null) return "";
        String first = u.getFirstName() != null ? u.getFirstName().trim() : "";
        String last = u.getLastName() != null ? u.getLastName().trim() : "";
        String full = (first + " " + last).trim();
        return full.isEmpty() ? (u.getUsername() != null ? u.getUsername() : "") : full;
    }

    // طريقتان ناجحتان:
    // ١) من System.getenv مباشرة (ستعمل إذا لم تستخدم dotenv)
    // ٢) أو، إذا عندك مكتبة dotenv، استخدمها (يمكنك إبقاء الطريقتين معاً بحسب المشروع)
    private static String getEnvCompanyEmail() {
        // جرّب أولاً من System.getenv
        String mail = System.getenv("EMAIL_USERNAME");
        if (mail != null && !mail.trim().isEmpty()) return mail.trim();

        // ثم جرّب من dotenv (في حال مكتبة Dotenv متوفرة)
        try {
            io.github.cdimascio.dotenv.Dotenv dotenv = io.github.cdimascio.dotenv.Dotenv.load();
            mail = dotenv.get("EMAIL_USERNAME");
            return mail != null ? mail.trim() : "";
        } catch (Throwable ex) {
            return "";
        }
    }
}