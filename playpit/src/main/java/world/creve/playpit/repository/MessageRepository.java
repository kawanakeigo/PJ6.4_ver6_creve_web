package world.creve.playpit.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import world.creve.playpit.dto.response.CreatorMessageCount;
import world.creve.playpit.dto.response.MessageRow;
import world.creve.playpit.entity.Message;

public interface MessageRepository extends JpaRepository<Message, Long>, JpaSpecificationExecutor<Message> {
    String ROW = "select new world.creve.playpit.dto.response.MessageRow(" +
            "m.messageId,m.creatorId,m.artworkId,m.message," +
            "case when m.anonymous=true then '匿名' when m.displayName is null or m.displayName='' then '来場者' else m.displayName end," +
            "m.createdAt,a.title,c.name) " +
            "from Message m left join Creator c on m.creatorId=c.creatorId left join Artwork a on m.artworkId=a.artworkId ";

    String PUBLIC_SCOPE = "where m.eventId=:eventId " +
            "and m.status=world.creve.playpit.entity.MessageStatus.PUBLISHED " +
            "and (m.creatorId is null or c.status='PUBLISHED') " +
            "and (m.artworkId is null or a.status='PUBLISHED') ";

    @Query(value = ROW + PUBLIC_SCOPE +
            "and (:creatorId is null or m.creatorId=:creatorId) " +
            "and (:artworkId is null or m.artworkId=:artworkId) " +
            "order by m.createdAt desc,m.messageId desc",
            countQuery = "select count(m) from Message m left join Creator c on m.creatorId=c.creatorId " +
                    "left join Artwork a on m.artworkId=a.artworkId " + PUBLIC_SCOPE +
                    "and (:creatorId is null or m.creatorId=:creatorId) " +
                    "and (:artworkId is null or m.artworkId=:artworkId)")
    Page<MessageRow> findFiltered(
            @Param("eventId") Long eventId,
            @Param("creatorId") Long creatorId,
            @Param("artworkId") Long artworkId,
            Pageable pageable);

    @Query("select count(m) from Message m left join Creator c on m.creatorId=c.creatorId " +
            "left join Artwork a on m.artworkId=a.artworkId " + PUBLIC_SCOPE)
    long countPublishedMessagesByEventId(@Param("eventId") Long eventId);

    @Query("select count(m) from Message m left join Creator c on m.creatorId=c.creatorId " +
            "left join Artwork a on m.artworkId=a.artworkId " + PUBLIC_SCOPE +
            "and (:creatorId is null or m.creatorId=:creatorId) " +
            "and (:artworkId is null or m.artworkId=:artworkId)")
    long countVisible(
            @Param("eventId") Long eventId,
            @Param("creatorId") Long creatorId,
            @Param("artworkId") Long artworkId);

    @Query("select new world.creve.playpit.dto.response.CreatorMessageCount(" +
            "c.creatorId,c.slug,c.name,count(m)) from Message m " +
            "join Creator c on m.creatorId=c.creatorId " +
            "left join Artwork a on m.artworkId=a.artworkId " + PUBLIC_SCOPE +
            "group by c.creatorId,c.slug,c.name order by c.name")
    List<CreatorMessageCount> creatorCounts(@Param("eventId") Long eventId);

    Page<Message> findAll(Specification<Message> specification, Pageable pageable);
}
