package com.newlegacyjam.platform.voting;

import com.newlegacyjam.platform.api.NljModels.VoteRequest;
import com.newlegacyjam.platform.api.NljModels.VoteResponse;
import com.newlegacyjam.platform.auth.LineIdTokenVerifier;
import com.newlegacyjam.platform.persistence.NljPersistencePort;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class VotingFacade {
    private final LineIdTokenVerifier verifier;
    private final NljPersistencePort persistence;
    private final VotingTransactionService transactions;

    public VotingFacade(LineIdTokenVerifier verifier, NljPersistencePort persistence, VotingTransactionService transactions) {
        this.verifier = verifier;
        this.persistence = persistence;
        this.transactions = transactions;
    }

    public VoteResponse vote(String token, VoteRequest request) {
        var identity = verifier.verify(token);
        long userId = persistence.upsertVerifiedUser(identity.subject(), identity.displayName(), Instant.now());
        return transactions.castVote(userId, request.eventId(), request.artistId(), request.expectedCycleId());
    }
}
