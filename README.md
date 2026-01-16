# Hyvote

A secure vote listener plugin for Hytale that enables voting sites to send vote notifications to your server.

## Features

- **Hybrid Encryption Protocol**: Uses RSA + AES-256 encryption for secure, flexible vote transmission
- **Configurable Actions**: Execute commands automatically when votes are received or when players join
- **Developer API**: Fire custom events and integrate vote handling into your own plugins
- **Replay Protection**: Built-in timestamp and nonce validation prevents duplicate votes
- **Multiple Protocol Support**: Version field allows for future protocol updates without breaking existing integrations

## Installation

1. Download the latest release
2. Place `Hyvote.jar` in your server's plugins folder
3. Start your server - `settings.json` will be generated automatically
4. Configure your settings (see below)
5. Share your public RSA key with voting sites (found in `keys/public.pem`)

## Configuration

Settings are stored in `settings.json`:

```json
{
  "port": 2250,
  "database": {
    "type": "sqlite",
    "host": "localhost",
    "port": 3306,
    "username": "hytale",
    "password": "supersecretdatabasepassword",
    "database": "hyvote"
  },
  "actions": [
    {
      "on": "vote",
      "type": "command",
      "command": "say ${player} just voted on ${voteSite}!"
    },
    {
      "on": "join",
      "type": "command",
      "command": "say Welcome back ${player}! Thanks for voting on ${voteSite}"
    }
  ]
}
```

### Configuration Options

**port**: The TCP port the vote listener binds to (default: 2250)

**database**: The configuration options for the database

**actions**: Array of actions to execute when votes are received

### Database Properties
Databases are used to track which nonces have been used and deliver votes for actions that require the user to be online but are recieved when offline.
- **type**: The database type to use (supported - MySQL, MariaDB, SQLite, PostgreSQL)
- **host**: The host of the database (ignored when using SQLite)
- **port**: The port that the database listens on (ignored when using SQLite)
- **username**: The username for the database user (ignored when using SQLite)
- **password**: The password for the database user (ignored when using SQLite)
- **database**: The database to use (ignored when using SQLite)

#### Action Properties

- **on**: When to execute the action
  - `vote` - Immediately after vote is processed
  - `join` - When the player joins (or immediately if already online)
  
- **type**: Type of action (currently only `command` is supported)

- **command**: The command to execute. Supports placeholders:
  - `${player}` - The voter's username
  - `${voteSite}` - The name of the voting site
  - `${uuid}` - The voter's UUID
  - `${timestamp}` - Vote timestamp

## Developer API

Hyvote provides an event system for plugin developers to integrate custom vote handling.

### Listening for Votes

```java
import uk.co.deveroonie.hyvote.api.VoteEventManager;
import uk.co.deveroonie.hyvote.api.VoteReceivedEvent;

// Register your listener
VoteEventManager.registerListener(event -> {
    Vote vote = event.getVote();
    
    // Access vote data
    String player = vote.playerName;
    String site = vote.voteSite;
    long timestamp = vote.timestamp;
    
    // Optionally cancel default processing
    event.setCancelled(true);
    
    // Implement your custom logic
    myPlugin.giveCustomReward(player);
});
```

### Vote Object

```java
public class Vote {
    public String uuid;          // Player UUID
    public String playerName;    // Player username
    public String voteSite;      // Voting site identifier
    public long timestamp;       // Unix timestamp
    public String nonce;         // Replay protection nonce
}
```

### Unregistering Listeners

```java
VoteListener myListener = event -> { /* ... */ };
VoteEventManager.registerListener(myListener);

// Later...
VoteEventManager.unregisterListener(myListener);
```

## Protocol Specification

Hyvote uses a custom binary protocol (HV01) for vote transmission:

### Message Format

```
[4 bytes] Magic: "HV01"
[4 bytes] Encrypted AES key length (big-endian int)
[N bytes] RSA-encrypted AES-256 key
[4 bytes] Encrypted payload length (big-endian int)
[M bytes] AES-encrypted JSON payload
```

### Encryption

1. Generate a random 256-bit AES key
2. Encrypt vote JSON with AES-256-CBC (include 16-byte IV prepended to ciphertext)
3. Encrypt the AES key with the server's RSA public key (PKCS1 or OAEP padding)
4. Package and send according to the message format above

### JSON Payload Structure

```json
{
  "uuid": "player-uuid-here",
  "playerName": "PlayerName",
  "voteSite": "VotingSiteName",
  "timestamp": 1737000000000,
  "nonce": "unique-random-string"
}
```

### Client Libraries

Official client libraries coming soon for:
- Java
- JavaScript/Node.js

## Security

- All vote data is encrypted end-to-end using hybrid RSA + AES encryption
- Replay attacks are mitigated through nonces
- Malformed messages are rejected before processing

## Support

For issues, feature requests, or protocol questions, please visit our GitHub repository.

## License

MIT
