package world.creve.platform.repository;
import java.util.*;
import java.time.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.domain.*;
import org.springframework.data.repository.query.Param;
import world.creve.platform.entity.*;
import world.creve.platform.dto.*;
public interface CreatorRepository extends JpaRepository<Creator,Long> {
    Optional<Creator> findBySlugAndStatus(String slug,String status);
    @Query(value="select new world.creve.platform.dto.CreatorSummaryResponse(c.creatorId,c.slug,c.name,c.profileImageUrl,c.genre,c.profile,(select count(a) from Artwork a where a.creatorId=c.creatorId and a.status='PUBLISHED')) from Creator c where c.status='PUBLISHED' order by c.name,c.creatorId",countQuery="select count(c) from Creator c where c.status='PUBLISHED'") Page<CreatorSummaryResponse> findPublishedCreators(Pageable pageable);
    @Query("select new world.creve.platform.dto.CreatorSummaryResponse(c.creatorId,c.slug,c.name,c.profileImageUrl,c.genre,c.profile,(select count(a) from Artwork a join EventArtwork ea on ea.artworkId=a.artworkId where ea.eventId=:eventId and a.creatorId=c.creatorId and a.status='PUBLISHED')) from Creator c join EventCreator ec on ec.creatorId=c.creatorId where ec.eventId=:eventId and c.status='PUBLISHED' order by ec.displayOrder,c.creatorId") List<CreatorSummaryResponse> findCreatorsByEventId(@Param("eventId") Long eventId,Pageable pageable);
    boolean existsBySlug(String slug);
    boolean existsBySlugAndCreatorIdNot(String slug,Long creatorId);
}
