package Test;

import persistence.DataRepository;
import Service.AuthService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * حالات فشل تسجيل الدخول / محاولات غير موجودة
 */
class AuthServiceNegativeLoginTest {

    @Test
    void login_fails_for_unknown_user_and_wrong_password() {
        DataRepository repo = new DataRepository();
        AuthService auth = new AuthService(repo);

        assertFalse(auth.login("noone", "pw"));
        auth.register("F","L","tester","Password1!", java.time.LocalDate.now().minusYears(20), "t@example.com");
        assertFalse(auth.login("tester", "wrongpw"));
    }
}