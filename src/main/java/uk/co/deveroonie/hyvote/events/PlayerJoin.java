package uk.co.deveroonie.hyvote.events;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import uk.co.deveroonie.hyvote.HyvotePlugin;
import uk.co.deveroonie.hyvote.models.Action;
import uk.co.deveroonie.hyvote.models.Vote;
import uk.co.deveroonie.hyvote.util.ProcessVote;

import java.sql.SQLException;
import java.util.List;

public class PlayerJoin {
    public static void onPlayerReady(PlayerReadyEvent event) {
        try {
            Player player = event.getPlayer();

            List<Vote> pendingVotes = HyvotePlugin.getConnection().getPendingVotesByPlayer(String.valueOf(player.getUuid()));

            if (!pendingVotes.isEmpty()) {
                for (Vote vote : pendingVotes) {
                    List<Action> actions = HyvotePlugin.getSettings().actions;

                    for (Action action : actions) {
                        if (action.on.equals("join")) {
                            ProcessVote.handleAction(action, vote);
                        }
                    }
                    
                    // Delete the pending vote after processing
                    if (vote.id != null) {
                        HyvotePlugin.getConnection().deletePendingVote(vote.id);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
