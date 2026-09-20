package com.newlegacyjam.platform.persistence;

import java.time.Instant;
import java.util.Optional;

public interface NljPersistencePort {
    void lockEvent(long eventId);
    Cycle resolveSingleActiveCycle(long eventId, Instant now);
    void requireEligibleArtist(long eventId, long artistId);
    Rule resolveRule(long eventId, long cycleId, Instant now);
    long upsertVerifiedUser(String lineSubject, String displayName, Instant now);
    long insertVote(long userId, long cycleId, long artistId, Instant now);
    void appendUserExp(long userId, long voteId, Rule rule, Instant now);
    void appendArtistExp(long artistId, long voteId, Rule rule, Instant now);
    Optional<VoteRecord> findVote(long userId, long cycleId);

    record Cycle(long id, Instant startsAt, Instant endsAt) {
    }
    record Rule(long id, long version, long userUnits, long artistUnits) {
    }
    record VoteRecord(long voteId, long artistId, Instant acceptedAt) {
    }
}
