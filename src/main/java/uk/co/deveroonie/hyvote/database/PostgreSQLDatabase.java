package uk.co.deveroonie.hyvote.database;

import uk.co.deveroonie.hyvote.HyvotePlugin;
import uk.co.deveroonie.hyvote.models.DatabaseProvider;
import uk.co.deveroonie.hyvote.models.Vote;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class PostgreSQLDatabase implements Database {
    private Connection connection;
    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final String database;

    public PostgreSQLDatabase(DatabaseProvider config) {
        this.host = config.host;
        this.port = config.port;
        this.username = config.username;
        this.password = config.password;
        this.database = config.database;
    }

    @Override
    public void connect() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            HyvotePlugin.getLog().at(Level.SEVERE).log("PostgreSQL JDBC driver not found");
            throw new RuntimeException("PostgreSQL JDBC driver not found", e);
        }
        connection = DriverManager.getConnection("jdbc:postgresql://"+host+":"+port+"/"+database,
                username, password);

    }

    @Override
    public void disconnect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Override
    public void savePendingVote(Vote vote, String uuid) throws SQLException {
        String sql = "INSERT INTO pendingvotes(uuid, player_name, vote_site, timestamp) VALUES(?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid);
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
                    vote.playerName = results.getString("player_name");
                    vote.voteSite = results.getString("vote_site");
                    vote.timestamp = results.getLong("timestamp");
                    vote.id = results.getInt("id");
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
                id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
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
               CREATE INDEX IF NOT EXISTS idx_nonce_timestamp ON nonces(timestamp);
            """;

        try (Statement statement = connection.createStatement()) {
            statement.execute(createPendingVotes);
            statement.execute(createNonces);
            statement.execute(createNonceIndex);
        }
    }
}