package Service;

public class TestEmailSender{
    public static void main(String[] args) {
        // بيانات الشركة
        String companyEmail = "remaajomaa842@gmail.com";
        String companyPassword = "wlmw flpp dkyd fguvهنا"; // أنصحك تستخدم App Password!

        // المستلم الأساسي
        String userEmail = "remaajomaa70@gmail.com";

        // أنشئ الخدمة (يفترض EmailService بعد التعديل)
        EmailService emailService = new EmailService(companyEmail, companyPassword);

        // بيانات الرسالة
        String subject = "رسالة تجريبية من تطبيق الحجز";
        String body = "هذه رسالة تجريبية للتأكد من عمل الإرسال بالإيميل عن طريق جافا :)";

        // إرسال الرسالة (سيصل TO إلى البريدين معاً)
        emailService.sendEmail(userEmail, subject, body);
    }
}