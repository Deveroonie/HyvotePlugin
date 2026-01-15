package uk.co.deveroonie.hyvote.database;

import uk.co.deveroonie.hyvote.models.DatabaseProvider;

public class DatabaseFactory {
    public static Database createDatabase(DatabaseProvider config) {
        return switch (config.type.toLowerCase()) {
            case "sqlite" -> new SQLiteDatabase(config);
            default -> throw new IllegalArgumentException("Unsupported database type: " + config.type);
        };
    }
}