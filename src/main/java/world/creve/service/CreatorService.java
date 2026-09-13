package world.creve.service;

import java.util.List;
import org.springframework.stereotype.Service;
import world.creve.entity.Creator;
import world.creve.entity.PublishStatus;
import world.creve.exception.NotFoundException;
import world.creve.repository.CreatorRepository;
import world.creve.repository.EventCreatorRepository;

@Service
public class CreatorService {
  private final CreatorRepository creatorRepository;
  private final EventCreatorRepository eventCreatorRepository;

  public CreatorService(
      CreatorRepository creatorRepository,
      EventCreatorRepository eventCreatorRepository
  ) {
    this.creatorRepository = creatorRepository;
    this.eventCreatorRepository = eventCreatorRepository;
  }

  public List<Creator> findAllForAdmin() {
    return creatorRepository.findAll();
  }

  public List<Creator> findPublishedCreators() {
    return creatorRepository.findByStatusOrderByDisplayOrderAsc(PublishStatus.PUBLISHED);
  }

  public List<Creator> findByEventId(String eventId) {
    return getCreatorsByEventId(eventId);
  }

  public List<Creator> getCreatorsByEventId(String eventId) {
    return creatorRepository.findCreatorsByEventId(eventId, PublishStatus.PUBLISHED);
  }

  public Creator findById(String creatorId) {
    return creatorRepository.findById(creatorId)
        .orElseThrow(() -> new NotFoundException("クリエイターが見つかりません。"));
  }

  public Creator findBySlug(String creatorSlug) {
    return creatorRepository.findBySlugAndStatus(creatorSlug, PublishStatus.PUBLISHED)
        .or(() -> creatorRepository.findById(creatorSlug)
            .filter(creator -> creator.getStatus() == PublishStatus.PUBLISHED))
        .orElseThrow(() -> new NotFoundException("クリエイターが見つかりません。"));
  }

  public Creator getCreatorForEvent(String eventId, String creatorSlug) {
    Creator creator = findBySlug(creatorSlug);
    if (!eventCreatorRepository.existsByEventIdAndCreatorId(eventId, creator.getId())) {
      throw new NotFoundException("イベント参加クリエイターが見つかりません。");
    }
    return creator;
  }

  public boolean isJoinedEvent(String eventId, String creatorId) {
    return eventCreatorRepository.existsByEventIdAndCreatorId(eventId, creatorId);
  }
}
