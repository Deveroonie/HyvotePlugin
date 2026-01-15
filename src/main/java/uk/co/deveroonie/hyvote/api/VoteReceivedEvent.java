package uk.co.deveroonie.hyvote.api;

import uk.co.deveroonie.hyvote.models.Vote;

public class VoteReceivedEvent {
    private final Vote vote;

    public VoteReceivedEvent(Vote vote) {
        this.vote = vote;
    }

    public Vote getVote() {
        return vote;
    }

}