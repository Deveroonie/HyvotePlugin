package uk.co.deveroonie.hyvote.api;

import uk.co.deveroonie.hyvote.models.Vote;

import java.util.ArrayList;
import java.util.List;

public class VoteEventManager {
    private static final List<VoteListener> listeners = new ArrayList<>();

    public static void registerListener(VoteListener listener) {
        listeners.add(listener);
    }

    public static void unregisterListener(VoteListener listener) {
        listeners.remove(listener);
    }

    public static VoteReceivedEvent fireEvent(Vote vote) {
        VoteReceivedEvent event = new VoteReceivedEvent(vote);
        for (VoteListener listener : listeners) {
            listener.onVoteReceived(event);
        }
        return event;
    }
}