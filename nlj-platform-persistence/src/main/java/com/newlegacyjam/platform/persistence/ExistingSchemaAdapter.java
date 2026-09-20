package com.newlegacyjam.platform.persistence;

import com.newlegacyjam.platform.api.NljException;
import java.time.Instant;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ExistingSchemaAdapter implements NljPersistencePort {
    private final JdbcTemplate jdbc;

    public ExistingSchemaAdapter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private NljException.Unavailable pending() {
        return new NljException.Unavailable("NLJ_SCHEMA_NOT_MAPPED",
                "既存NLJのV001〜V015と実DDLの照合が完了するまで投票は開始できません。");
    }

    @Override public void lockEvent(long eventId) { throw pending(); }
    @Override public Cycle resolveSingleActiveCycle(long eventId, Instant now) { throw pending(); }
    @Override public void requireEligibleArtist(long eventId, long artistId) { throw pending(); }
    @Override public Rule resolveRule(long eventId, long cycleId, Instant now) { throw pending(); }
    @Override public long upsertVerifiedUser(String lineSubject, String displayName, Instant now) { throw pending(); }
    @Override public long insertVote(long userId, long cycleId, long artistId, Instant now) { throw pending(); }
    @Override public void appendUserExp(long userId, long voteId, Rule rule, Instant now) { throw pending(); }
    @Override public void appendArtistExp(long artistId, long voteId, Rule rule, Instant now) { throw pending(); }
    @Override public Optional<VoteRecord> findVote(long userId, long cycleId) { throw pending(); }
}
