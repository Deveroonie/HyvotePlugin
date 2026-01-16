package uk.co.deveroonie.hyvote.models;

public class Vote {
    public Integer id; // Database ID for pending votes (null for new votes)
    public String playerName;
    public String voteSite;
    public long timestamp;
    public String nonce;
}
