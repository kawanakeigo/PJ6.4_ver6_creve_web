package world.creve.platform;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import world.creve.platform.service.CreatorRanking;
import world.creve.platform.dto.*;
import java.time.*;
import java.util.*;
class CreatorRankingTest {
 Clock clock=Clock.fixed(Instant.parse("2026-06-01T00:00:00Z"),ZoneOffset.UTC);
 CreatorSummaryResponse c(long id,String name){return new CreatorSummaryResponse(id,"c"+id,name,null,null,null,0);}
 RankingParticipation p(long c,long e,int year,String space){return new RankingParticipation(c,e,LocalDateTime.of(year,6,1,0,0),space);}
 @Test void floorFutureUnknownAndJapanYearBoundary(){
  assertEquals(0,CreatorRanking.spacePoints("SMALL",2024,clock));
  assertEquals(4,CreatorRanking.spacePoints("LARGE",2028,clock));
  assertEquals(0,CreatorRanking.spacePoints(null,2026,clock));
  assertEquals(0,CreatorRanking.spacePoints("①",2026,clock));
  assertEquals(4,CreatorRanking.spacePoints("LARGE",2026,Clock.fixed(Instant.parse("2026-12-31T14:59:59Z"),ZoneOffset.UTC)));
  assertEquals(3,CreatorRanking.spacePoints("LARGE",2026,Clock.fixed(Instant.parse("2026-12-31T15:00:00Z"),ZoneOffset.UTC)));
 }
 @Test void latestDateBeforeScoreThenNames(){
  var result=CreatorRanking.sort(List.of(c(1,"Z"),c(2,"A"),c(3,"B"),c(4,"C")),List.of(p(1,1,2027,null),p(2,2,2026,"SMALL"),p(3,3,2026,"LARGE")),clock);
  assertEquals(List.of(1L,3L,2L,4L),result.stream().map(CreatorSummaryResponse::creatorId).toList());
 }
 @Test void aggregateUniqueEventsAndNameTies(){
  var rows=List.of(p(1,1,2026,"MEDIUM"),p(1,1,2026,"MEDIUM"),p(2,2,2026,"SMALL"),p(2,3,2025,"LARGE"),p(3,4,2026,"MEDIUM"));
  var result=CreatorRanking.sort(List.of(c(1,"Z"),c(2,"B"),c(3,"A")),rows,clock);
  assertEquals(List.of(2L,3L,1L),result.stream().map(CreatorSummaryResponse::creatorId).toList());
 }
 @Test void sameCalendarDateIgnoresTimeAndCountsUnknownParticipation(){
  var rows=List.of(new RankingParticipation(1L,1L,LocalDateTime.of(2026,6,1,23,0),null),p(2,2,2026,"SMALL"));
  assertEquals(2L,CreatorRanking.sort(List.of(c(1,"A"),c(2,"B")),rows,clock).getFirst().creatorId());
 }
}
