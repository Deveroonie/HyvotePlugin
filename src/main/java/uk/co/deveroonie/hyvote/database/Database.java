package uk.co.deveroonie.hyvote.database;

import uk.co.deveroonie.hyvote.models.Vote;

import java.sql.SQLException;
import java.util.List;

public interface Database {
    void connect() throws SQLException;
    void disconnect() throws SQLException;
    void savePendingVote(Vote vote, String uuid) throws SQLException;
    void saveNonce(String nonce) throws SQLException;
    boolean nonceExists(String nonce) throws SQLException;
    List<Vote> getPendingVotesByPlayer(String uuid) throws SQLException;

    void deletePendingVote(int voteId) throws SQLException;

    void cleanupOldNonces(long olderThanMillis) throws SQLException;

    void initialize() throws SQLException; // Create tables if needed
}