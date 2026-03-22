package Test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import persistence.RepoStorage;
import persistence.DataRepository;
import domain.Category;
import domain.User;
import domain.Provider;
import domain.TimeSlot;
import domain.Appointment;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for persistence.RepoStorage
 */
class RepoStorageTest {

    @Test
    void defaultPath_is_under_user_home_and_contains_expected_names() {
        Path p = RepoStorage.defaultPath();
        String str = p.toString();
        String home = System.getProperty("user.home");
        assertTrue(str.contains(home), "defaultPath should be inside user.home");
        assertTrue(str.contains(".Soft_Proj"), "defaultPath should include .Soft_Proj directory");
        assertTrue(str.endsWith("data.json"), "defaultPath should end with data.json");
    }

    @Test
    void loadOrNew_returns_new_repo_when_file_missing(@TempDir Path tempDir) {
        Path path = tempDir.resolve("no-such-dir").resolve("data.json");
        assertFalse(Files.exists(path));
        DataRepository repo = RepoStorage.loadOrNew(path);
        assertNotNull(repo);
        assertTrue(repo.getUsers().isEmpty());
        assertTrue(repo.getCategories().isEmpty());
    }

    @Test
    void loadOrNew_returns_new_repo_when_file_empty(@TempDir Path tempDir) throws Exception {
        Path file = tempDir.resolve("empty.json");
        Files.createDirectories(file.getParent());
        Files.writeString(file, "   "); // empty content
        DataRepository repo = RepoStorage.loadOrNew(file);
        assertNotNull(repo);
        assertTrue(repo.getUsers().isEmpty());
        assertTrue(repo.getCategories().isEmpty());
    }

    @Test
    void loadOrNew_returns_new_repo_on_invalid_json(@TempDir Path tempDir) throws Exception {
        Path file = tempDir.resolve("invalid.json");
        Files.createDirectories(file.getParent());
        Files.writeString(file, "this is not json");
        DataRepository repo = RepoStorage.loadOrNew(file);
        assertNotNull(repo);
        assertTrue(repo.getUsers().isEmpty());
        assertTrue(repo.getCategories().isEmpty());
    }

    @Test
    void save_and_load_roundtrip_persists_repository(@TempDir Path tempDir) throws Exception {
        Path file = tempDir.resolve("store").resolve("data.json");

        DataRepository repo = new DataRepository();

        Category c = new Category("MyCategory");
        repo.addCategory(c);

        User u = new User("alice", "pw");
        repo.addUser(u);

        Provider p = new Provider("provX", "pw", "Clinic", "", "prov@example.com", "Addr");
        repo.addProvider(p);

        TimeSlot slot = new TimeSlot(LocalDateTime.now().plusDays(1), 60, c);
        repo.addSlot(slot);

        Appointment ap = new Appointment(u, slot, 30, 1);
        repo.addAppointment(ap);

        assertFalse(Files.exists(file));

        RepoStorage.save(file, repo);

        assertTrue(Files.exists(file), "save should create the JSON file");
        String json = Files.readString(file);
        assertNotNull(json);
        assertFalse(json.trim().isEmpty());

        DataRepository loaded = RepoStorage.loadOrNew(file);
        assertNotNull(loaded);
        assertTrue(loaded.getCategories().stream().anyMatch(cat -> "MyCategory".equals(cat.getName())));
        assertTrue(loaded.getUsers().stream().anyMatch(us -> "alice".equals(us.getUsername())));
        assertTrue(loaded.getProviders().stream().anyMatch(pr -> "provX".equals(pr.getUsername())));
        assertTrue(loaded.getSlots().size() >= 1);
        assertTrue(loaded.getAppointments().stream().anyMatch(a -> a.getDurationInMinutes() == ap.getDurationInMinutes()));
    }

    @Test
    void save_handles_null_inputs_and_does_not_throw(@TempDir Path tempDir) {
        assertDoesNotThrow(() -> RepoStorage.save((Path) null, new DataRepository()));
        Path file = tempDir.resolve("out.json");
        assertDoesNotThrow(() -> RepoStorage.save(file, null));
    }

    @Test
    void save_handles_ioexception_when_parent_is_file(@TempDir Path tempDir) throws Exception {
        Path parentFile = tempDir.resolve("notADir");
        Files.createFile(parentFile); 
        Path target = parentFile.resolve("data.json"); 
        DataRepository repo = new DataRepository();
        repo.addCategory(new Category("X"));

        assertDoesNotThrow(() -> RepoStorage.save(target, repo));
        assertFalse(Files.exists(target), "When parent is a file, save should not create the data file");
    }
}