package uk.co.deveroonie.hyvote.util;

import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.console.ConsoleSender;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import org.apache.commons.lang3.StringUtils;
import tools.jackson.databind.ObjectMapper;
import uk.co.deveroonie.hyvote.HyvotePlugin;
import uk.co.deveroonie.hyvote.api.VoteEventManager;
import uk.co.deveroonie.hyvote.models.Action;
import uk.co.deveroonie.hyvote.models.Vote;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;

public class ProcessVote {
    static CommandManager manager = CommandManager.get();
    static CommandSender console = ConsoleSender.INSTANCE;


    public ProcessVote(byte[] voteMessage) throws SQLException {
        ObjectMapper objectMapper = new ObjectMapper();

        Vote vote = objectMapper.readValue(voteMessage, Vote.class);

        // Make sure it isn't malformed
        if (isEmpty(vote.uuid) || isEmpty(vote.playerName) || StringUtils.isEmpty(vote.voteSite) || isEmpty(vote.timestamp) || StringUtils.isEmpty(vote.nonce)) {
            HyvotePlugin.getLog().at(Level.INFO).log("Failed to process malformed vote.");
            return;
        }

        if (HyvotePlugin.getConnection().nonceExists(vote.nonce)) return;
        HyvotePlugin.getConnection().saveNonce(vote.nonce);

        List<Action> actions = HyvotePlugin.getSettings().actions;

        // Check if the player is online. If they are, we can do all actions
        PlayerRef player = Universe.get().getPlayerByUsername(vote.playerName, NameMatching.EXACT_IGNORE_CASE);
        VoteEventManager.fireEvent(vote);
        if(player == null) {
            // Check if there are any actions that require the player to be online
            boolean hasNonVoteActions = actions.stream().anyMatch(action -> !Objects.equals(action.on, "vote"));
            if(hasNonVoteActions) {
                HyvotePlugin.getConnection().savePendingVote(vote);
            }
            
            // Only do onVote actions
            for (Action action : actions) {
                if(Objects.equals(action.on, "vote")) {
                    handleAction(action, vote);
                }
            }
        } else {
            for (Action action : actions) {
                handleAction(action, vote, player);
            }
        }
    }

    public static void handleAction(Action action, Vote vote, PlayerRef... player) {
        if(Objects.equals(action.type, "command")) {
            Map<String, String> valuesMap = new HashMap<>();
            valuesMap.put("player", vote.playerName);
            valuesMap.put("voteSite", vote.voteSite);

            manager.handleCommand(console, action.command);
        }
    }
}
