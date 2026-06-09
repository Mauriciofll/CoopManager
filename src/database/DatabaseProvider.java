package database;

public class DatabaseProvider {
    private static DatabaseGateway database;

    private DatabaseProvider() {
    }

    public static synchronized DatabaseGateway getDatabase() {
        if (database == null) {
            DatabaseConfig config = DatabaseConfig.load();
            database = config.isPostgres() ? new PostgresDatabase(config) : LocalDatabase.getInstance();
        }
        return database;
    }
}
