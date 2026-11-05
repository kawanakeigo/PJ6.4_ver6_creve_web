package world.creve.platform.dto;
import java.time.LocalDateTime;
public record RankingParticipation(Long creatorId,Long eventId,LocalDateTime startAt,String spaceType) {}
