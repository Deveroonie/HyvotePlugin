package uk.co.deveroonie.hyvote.database;

import uk.co.deveroonie.hyvote.HyvotePlugin;
import uk.co.deveroonie.hyvote.models.DatabaseProvider;
import uk.co.deveroonie.hyvote.models.Vote;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SQLiteDatabase implements Database {
    private Connection connection;
    private final String filePath;

    public SQLiteDatabase(DatabaseProvider config) {
        this.filePath = new File(HyvotePlugin.getDataDir() + "database.db").getAbsolutePath();
    }

    @Override
    public void connect() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:" + filePath);
    }

    @Override
    public void disconnect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Override
    public void savePendingVote(Vote vote) throws SQLException {
        String sql = "INSERT INTO pendingvotes(uuid, player_name, vote_site, timestamp) VALUES(?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, vote.uuid);
            statement.setString(2, vote.playerName);
            statement.setString(3, vote.voteSite);
            statement.setLong(4, vote.timestamp);
            statement.executeUpdate();
        }
    }

    @Override
    public void saveNonce(String nonce) throws SQLException {
        String sql = "INSERT INTO nonces(nonce, timestamp) VALUES(?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, nonce);
            statement.setLong(2, System.currentTimeMillis());
            statement.executeUpdate();
        }
    }

    @Override
    public boolean nonceExists(String nonce) throws SQLException {
        String sql = "SELECT 1 FROM nonces WHERE nonce = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, nonce);

            try (ResultSet results = statement.executeQuery()) {
                return results.next();
            }
        }
    }

    @Override
    public List<Vote> getPendingVotesByPlayer(String uuid) throws SQLException {
        String sql = "SELECT * FROM pendingvotes WHERE uuid = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid);

            List<Vote> pendingVotes = new ArrayList<>();

            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    Vote vote = new Vote();
                    vote.id = results.getInt("id");
                    vote.uuid = results.getString("uuid");
                    vote.playerName = results.getString("player_name");
                    vote.voteSite = results.getString("vote_site");
                    vote.timestamp = results.getLong("timestamp");
                    pendingVotes.add(vote);
                }
            }

            return pendingVotes;
        }
    }

    @Override
    public void deletePendingVote(int voteId) throws SQLException {
        String sql = "DELETE FROM pendingvotes WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, voteId);
            statement.executeUpdate();
        }
    }

    @Override
    public void cleanupOldNonces(long olderThanMillis) throws SQLException {
        String sql = "DELETE FROM nonces WHERE timestamp < ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, System.currentTimeMillis() - olderThanMillis);
            statement.executeUpdate();
        }
    }

    @Override
    public void initialize() throws SQLException {
        String createPendingVotes = """
            CREATE TABLE IF NOT EXISTS pendingvotes (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                uuid TEXT NOT NULL,
                player_name TEXT NOT NULL,
                vote_site TEXT NOT NULL,
                timestamp BIGINT NOT NULL
            )
            """;

        String createNonces = """
            CREATE TABLE IF NOT EXISTS nonces (
                nonce TEXT PRIMARY KEY NOT NULL,
                timestamp BIGINT NOT NULL
            )
            """;

        String createNonceIndex = """
            CREATE INDEX IF NOT EXISTS idx_nonce_timestamp ON nonces(timestamp)
            """;

        try (Statement statement = connection.createStatement()) {
            statement.execute(createPendingVotes);
            statement.execute(createNonces);
            statement.execute(createNonceIndex);
        }
    }
}