package persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import persistence.dto.RepoSnapshot;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Persists and restores {@link DataRepository} to/from disk using JSON snapshots.
 *
 * <p>The storage location defaults to a file named {@value #FILE_NAME} inside a hidden directory
 * named {@value #DIR_NAME} under the current user's home directory (as defined by {@code user.home}).</p>
 */
public final class RepoStorage {

    private static final String DIR_NAME = ".Soft_Proj";
    private static final String FILE_NAME = "data.json";

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .enable(SerializationFeature.INDENT_OUTPUT);

    private RepoStorage() {}

    /**
     * Returns the default repository file path under the user's home directory.
     *
     * @return default path to the JSON data file
     */
    public static Path defaultPath() {
        String home = System.getProperty("user.home");
        return Path.of(home, DIR_NAME, FILE_NAME);
    }

    /**
     * Loads the repository from {@link #defaultPath()} or returns a new empty repository if not found/invalid.
     *
     * @return loaded repository or a new instance if loading fails
     */
    public static DataRepository loadOrNew() {
        return loadOrNew(defaultPath());
    }

    /**
     * Loads the repository from the given path or returns a new empty repository if not found/invalid.
     *
     * @param path JSON file path to load from
     * @return loaded repository or a new instance if loading fails
     */
    public static DataRepository loadOrNew(Path path) {
        try {
            System.out.println("[RepoStorage] load path = " + path.toAbsolutePath());

            if (path == null || !Files.exists(path)) {
                System.out.println("[RepoStorage] file not found -> new repo");
                return new DataRepository();
            }

            String json = Files.readString(path);
            if (json == null || json.trim().isEmpty()) {
                System.out.println("[RepoStorage] file empty -> new repo");
                return new DataRepository();
            }

            RepoSnapshot snap = MAPPER.readValue(json, RepoSnapshot.class);
            System.out.println("[RepoStorage] loaded OK");
            return RepoSnapshotMapper.toRepo(snap);

        } catch (Exception ex) {
            System.out.println("[RepoStorage] load FAILED: " + ex.getMessage());
            return new DataRepository();
        }
    }

    /**
     * Saves the repository to {@link #defaultPath()}.
     *
     * @param repo repository to save
     */
    public static void save(DataRepository repo) {
        save(defaultPath(), repo);
    }

    /**
     * Saves the repository to the given file path.
     *
     * @param path destination JSON file path
     * @param repo repository to save
     */
    public static void save(Path path, DataRepository repo) {
        if (path == null || repo == null) return;

        try {
            System.out.println("[RepoStorage] save path = " + path.toAbsolutePath());

            Files.createDirectories(path.getParent());

            RepoSnapshot snap = RepoSnapshotMapper.fromRepo(repo);
            String json = MAPPER.writeValueAsString(snap);

            Files.writeString(path, json);

            System.out.println("[RepoStorage] save OK (bytes=" + json.length() + ")");
        } catch (IOException ex) {
            System.out.println("[RepoStorage] save FAILED: " + ex.getMessage());
        }
    }
}