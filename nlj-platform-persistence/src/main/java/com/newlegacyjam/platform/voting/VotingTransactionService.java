package com.newlegacyjam.platform.voting;

import com.newlegacyjam.platform.api.NljException;
import com.newlegacyjam.platform.api.NljModels.VoteResponse;
import com.newlegacyjam.platform.persistence.NljPersistencePort;
import java.time.Clock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VotingTransactionService {
    private final NljPersistencePort persistence;
    private final Clock clock;

    public VotingTransactionService(NljPersistencePort persistence, Clock clock) {
        this.persistence = persistence;
        this.clock = clock;
    }

    @Transactional
    public VoteResponse castVote(long userId, long eventId, long artistId, long expectedCycleId) {
        persistence.lockEvent(eventId);
        var now = clock.instant();
        var cycle = persistence.resolveSingleActiveCycle(eventId, now);
        if (cycle.id() != expectedCycleId) {
            throw new NljException.Conflict("CYCLE_CHANGED", "投票期間が切り替わりました。画面を更新してください。");
        }
        if (now.isBefore(cycle.startsAt()) || !now.isBefore(cycle.endsAt())) {
            throw new NljException.Conflict("VOTING_CLOSED", "現在は投票を受け付けていません。");
        }
        persistence.requireEligibleArtist(eventId, artistId);
        var rule = persistence.resolveRule(eventId, cycle.id(), now);
        long voteId = persistence.insertVote(userId, cycle.id(), artistId, now);
        persistence.appendUserExp(userId, voteId, rule, now);
        persistence.appendArtistExp(artistId, voteId, rule, now);
        return new VoteResponse(voteId, cycle.id(), rule.userUnits(), rule.artistUnits(), now);
    }
}
