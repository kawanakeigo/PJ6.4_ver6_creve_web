package com.newlegacyjam.platform.api;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.Instant;
import java.util.List;

public final class NljModels {
    private NljModels() {
    }

    public record ArtistCard(long artistId, String name, String profile, String side, int position) {
    }

    public record PublicEventCard(long eventId, String title, String summary, Instant startsAt, Instant endsAt,
                                  long activeCycleId, Instant votingEndsAt, boolean acceptingVotes) {
    }

    public record EventDetail(PublicEventCard event, List<ArtistCard> artists) {
    }

    public record PageResult<T>(List<T> items, int page, int size, long total, boolean hasNext) {
    }

    public record VoteRequest(@NotNull @Positive Long eventId, @NotNull @Positive Long artistId,
                              @NotNull @Positive Long expectedCycleId) {
    }

    public record VoteResponse(long voteId, long cycleId, long userExpUnits, long artistExpUnits,
                               Instant acceptedAt) {
    }

    public record VoteStatusResponse(long cycleId, boolean voted, Long voteId, Long artistId, Instant acceptedAt) {
    }

    public record ExpBalance(long units, String exp) {
    }
}
