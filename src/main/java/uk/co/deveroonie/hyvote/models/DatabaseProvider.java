package uk.co.deveroonie.hyvote.models;

public class DatabaseProvider {
    public String type;
    public String host;
    public int port;
    public String database;
    public String username;
    public String password;

    // Connection pool settings
    public int maxConnections = 10;
    public int minConnections = 2;
}