package uk.co.deveroonie.hyvote.database;

import uk.co.deveroonie.hyvote.models.DatabaseProvider;

public class DatabaseFactory {
    public static Database createDatabase(DatabaseProvider config) {
        return switch (config.type.toLowerCase()) {
            case "sqlite" -> new SQLiteDatabase(config);
            case "mysql" -> new MYSQLDatabase(config);
            case "mariadb" -> new MariaDBDatabase(config);
            case "postgresql" -> new PostgreSQLDatabase(config);
            default -> throw new IllegalArgumentException("Unsupported database type: " + config.type);
        };
    }
}