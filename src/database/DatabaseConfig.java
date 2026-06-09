package database;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class DatabaseConfig {
    private static final String APPLICATION_DIRECTORY = "CoopManager";
    private static final String CONFIG_FILE = "database.properties";

    private final String mode;
    private final String url;
    private final String user;
    private final String password;
    private final Path sourcePath;

    private DatabaseConfig(String mode, String url, String user, String password, Path sourcePath) {
        this.mode = mode;
        this.url = url;
        this.user = user;
        this.password = password;
        this.sourcePath = sourcePath;
    }

    public static DatabaseConfig load() {
        Properties properties = new Properties();
        Path path = findConfigPath();

        if (path != null) {
            try (FileInputStream input = new FileInputStream(path.toFile())) {
                properties.load(input);
            } catch (IOException exception) {
                throw new IllegalStateException("Não foi possível ler a configuração do banco: " + path, exception);
            }
        }

        String mode = properties.getProperty("database.mode", "local").trim().toLowerCase();
        String url = properties.getProperty("postgres.url", "jdbc:postgresql://localhost:5432/coopmanager").trim();
        String user = properties.getProperty("postgres.user", "coopmanager").trim();
        String password = properties.getProperty("postgres.password", "").trim();

        return new DatabaseConfig(mode, url, user, password, path);
    }

    public boolean isPostgres() {
        return "postgres".equals(mode) || "postgresql".equals(mode);
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getMode() {
        return mode;
    }

    public Path getSourcePath() {
        return sourcePath;
    }

    public static boolean userConfigExists() {
        return Files.exists(getUserConfigPath());
    }

    public static void saveLocal() {
        save("local", "jdbc:postgresql://localhost:5432/coopmanager", "coopmanager", "");
    }

    public static void savePostgres(String url, String user, String password) {
        save("postgres", url, user, password);
    }

    public static Path getUserConfigPath() {
        String appData = System.getenv("APPDATA");
        if (appData != null && !appData.isBlank()) {
            return Path.of(appData, APPLICATION_DIRECTORY, CONFIG_FILE);
        }

        String userHome = System.getProperty("user.home");
        if (userHome != null && !userHome.isBlank()) {
            return Path.of(userHome, "." + APPLICATION_DIRECTORY.toLowerCase(), CONFIG_FILE);
        }

        return Path.of("config", CONFIG_FILE);
    }

    private static void save(String mode, String url, String user, String password) {
        Properties properties = new Properties();
        properties.setProperty("database.mode", mode == null ? "local" : mode.trim());
        properties.setProperty("postgres.url", url == null ? "" : url.trim());
        properties.setProperty("postgres.user", user == null ? "" : user.trim());
        properties.setProperty("postgres.password", password == null ? "" : password);

        Path path = getUserConfigPath();
        try {
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            try (FileOutputStream output = new FileOutputStream(path.toFile())) {
                properties.store(output, "CoopManager database configuration");
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Não foi possível salvar a configuração do banco: " + path, exception);
        }
    }

    private static Path findConfigPath() {
        Path userPath = getUserConfigPath();
        if (Files.exists(userPath)) {
            return userPath;
        }

        Path projectPath = Path.of("config", CONFIG_FILE);
        if (Files.exists(projectPath)) {
            return projectPath;
        }

        return null;
    }
}
