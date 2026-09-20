package com.newlegacyjam.platform.voting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.newlegacyjam.platform.api.NljException;
import com.newlegacyjam.platform.persistence.NljPersistencePort;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class VotingTransactionServiceTest {
    private static final Instant NOW = Instant.parse("2026-09-20T00:00:00Z");

    @Test
    void voteAndBothLedgersUseTheSameVoteReference() {
        FakePort port = new FakePort(31);
        var service = new VotingTransactionService(port, Clock.fixed(NOW, ZoneOffset.UTC));

        var result = service.castVote(7, 11, 22, 31);

        assertEquals(9001, result.voteId());
        assertEquals(250, result.userExpUnits());
        assertEquals(500, result.artistExpUnits());
        assertEquals(List.of("lock:11", "cycle:11", "artist:22", "rule:31", "vote:7:31:22",
                "user-exp:7:9001", "artist-exp:22:9001"), port.calls);
    }

    @Test
    void changedCycleStopsBeforeVoteAndExp() {
        FakePort port = new FakePort(32);
        var service = new VotingTransactionService(port, Clock.fixed(NOW, ZoneOffset.UTC));

        NljException.Conflict error = assertThrows(NljException.Conflict.class,
                () -> service.castVote(7, 11, 22, 31));

        assertEquals("CYCLE_CHANGED", error.code());
        assertEquals(List.of("lock:11", "cycle:11"), port.calls);
    }

    @Test
    void cycleEndIsExclusive() {
        FakePort port = new FakePort(31, NOW.minusSeconds(60), NOW);
        var service = new VotingTransactionService(port, Clock.fixed(NOW, ZoneOffset.UTC));

        NljException.Conflict error = assertThrows(NljException.Conflict.class,
                () -> service.castVote(7, 11, 22, 31));

        assertEquals("VOTING_CLOSED", error.code());
        assertEquals(List.of("lock:11", "cycle:11"), port.calls);
    }

    private static final class FakePort implements NljPersistencePort {
        private final long cycle;
        private final Instant startsAt;
        private final Instant endsAt;
        private final List<String> calls = new ArrayList<>();

        private FakePort(long cycle) {
            this(cycle, NOW.minusSeconds(60), NOW.plusSeconds(60));
        }

        private FakePort(long cycle, Instant startsAt, Instant endsAt) {
            this.cycle = cycle;
            this.startsAt = startsAt;
            this.endsAt = endsAt;
        }
        @Override public void lockEvent(long eventId) { calls.add("lock:" + eventId); }
        @Override public Cycle resolveSingleActiveCycle(long eventId, Instant now) {
            calls.add("cycle:" + eventId);
            return new Cycle(cycle, startsAt, endsAt);
        }
        @Override public void requireEligibleArtist(long eventId, long artistId) { calls.add("artist:" + artistId); }
        @Override public Rule resolveRule(long eventId, long cycleId, Instant now) {
            calls.add("rule:" + cycleId);
            return new Rule(5, 2, 250, 500);
        }
        @Override public long upsertVerifiedUser(String lineSubject, String displayName, Instant now) { return 7; }
        @Override public long insertVote(long userId, long cycleId, long artistId, Instant now) {
            calls.add("vote:" + userId + ":" + cycleId + ":" + artistId);
            return 9001;
        }
        @Override public void appendUserExp(long userId, long voteId, Rule rule, Instant now) {
            calls.add("user-exp:" + userId + ":" + voteId);
        }
        @Override public void appendArtistExp(long artistId, long voteId, Rule rule, Instant now) {
            calls.add("artist-exp:" + artistId + ":" + voteId);
        }
        @Override public Optional<VoteRecord> findVote(long userId, long cycleId) { return Optional.empty(); }
    }
}
