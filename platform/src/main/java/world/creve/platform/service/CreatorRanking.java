package world.creve.platform.service;
import java.time.*;
import java.util.*;
import world.creve.platform.dto.*;
/** Public PLAYPIT participation only; no artwork join or persisted score. */
public final class CreatorRanking {
 private CreatorRanking() {}
 public static int spacePoints(String type,int eventYear,Clock clock) {
  int base=switch(type==null?"":type){case "LARGE"->4;case "ATELIER"->3;case "MEDIUM"->2;case "SMALL"->1;default->0;};
  int year=LocalDate.now(clock.withZone(ZoneId.of("Asia/Tokyo"))).getYear();
  return Math.max(0,base-Math.max(0,year-eventYear));
 }
 public static List<CreatorSummaryResponse> sort(List<CreatorSummaryResponse> creators,List<RankingParticipation> rows,Clock clock) {
  Map<Long,LocalDate> latest=new HashMap<>();Map<Long,Integer> scores=new HashMap<>();Set<String> seen=new HashSet<>();
  for(var row:rows){if(!seen.add(row.creatorId()+":"+row.eventId()))continue;
   latest.merge(row.creatorId(),row.startAt().toLocalDate(),(a,b)->a.isAfter(b)?a:b);
   scores.merge(row.creatorId(),1+spacePoints(row.spaceType(),row.startAt().getYear(),clock),Integer::sum);
  }
  var result=new ArrayList<>(creators);
  result.sort(Comparator.comparing((CreatorSummaryResponse c)->latest.get(c.creatorId()),Comparator.nullsLast(Comparator.reverseOrder()))
   .thenComparing(Comparator.comparingInt((CreatorSummaryResponse c)->scores.getOrDefault(c.creatorId(),0)).reversed())
   .thenComparing(CreatorSummaryResponse::name).thenComparing(CreatorSummaryResponse::creatorId));
  return result;
 }
}
