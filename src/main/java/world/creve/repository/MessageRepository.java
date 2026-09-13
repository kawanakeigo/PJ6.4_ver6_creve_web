package world.creve.repository;

import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import world.creve.entity.Message;
import world.creve.entity.MessageStatus;

public interface MessageRepository extends JpaRepository<Message, Long> {
  List<Message> findAllByOrderByCreatedAtDesc();

  @Query("""
      select message from Message message
      where message.eventId = :eventId
        and message.status = :status
      order by message.createdAt desc
      """)
  List<Message> findPublishedMessagesByEventId(
      @Param("eventId") String eventId,
      @Param("status") MessageStatus status
  );

  @Query("""
      select message from Message message
      where message.eventId = :eventId
        and message.status = :status
      order by message.createdAt desc
      """)
  List<Message> findPublishedMessagesByEventId(
      @Param("eventId") String eventId,
      @Param("status") MessageStatus status,
      Pageable pageable
  );

  @Query("""
      select message from Message message
      where message.eventId = :eventId
        and message.creatorId = :creatorId
        and message.status = :status
      order by message.createdAt desc
      """)
  List<Message> findPublishedMessagesByCreatorId(
      @Param("eventId") String eventId,
      @Param("creatorId") String creatorId,
      @Param("status") MessageStatus status
  );

  @Query("""
      select message from Message message
      where message.eventId = :eventId
        and message.artworkId = :artworkId
        and message.status = :status
      order by message.createdAt desc
      """)
  List<Message> findPublishedMessagesByArtworkId(
      @Param("eventId") String eventId,
      @Param("artworkId") String artworkId,
      @Param("status") MessageStatus status
  );

  long countByEventIdAndStatus(String eventId, MessageStatus status);

  long countByEventIdAndCreatorIdAndStatus(String eventId, String creatorId, MessageStatus status);

  long countByEventIdAndArtworkIdAndStatus(String eventId, String artworkId, MessageStatus status);

  long countBySessionHashAndCreatedAtAfter(String sessionHash, OffsetDateTime createdAfter);

  boolean existsBySessionHashAndEventIdAndBodyAndCreatedAtAfter(
      String sessionHash,
      String eventId,
      String body,
      OffsetDateTime createdAfter
  );

  @Query("""
      select message from Message message
      where (:eventId is null or message.eventId = :eventId)
        and (:creatorId is null or message.creatorId = :creatorId)
        and (:artworkId is null or message.artworkId = :artworkId)
        and (:status is null or message.status = :status)
        and (:keyword is null or lower(message.body) like lower(concat('%', :keyword, '%'))
          or lower(message.displayName) like lower(concat('%', :keyword, '%')))
      order by message.createdAt desc
      """)
  List<Message> searchMessages(
      @Param("eventId") String eventId,
      @Param("creatorId") String creatorId,
      @Param("artworkId") String artworkId,
      @Param("status") MessageStatus status,
      @Param("keyword") String keyword
  );
}
