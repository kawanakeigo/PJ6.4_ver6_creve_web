package world.creve.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import world.creve.dto.response.PetalResponse;
import world.creve.dto.response.UkaResponse;
import world.creve.entity.Event;
import world.creve.entity.MessageStatus;
import world.creve.repository.MessageRepository;
import world.creve.util.PetalLayoutUtil;

@Service
public class UkaService {
  private final EventService eventService;
  private final MessageRepository messageRepository;
  private final PetalLayoutUtil petalLayoutUtil;

  public UkaService(
      EventService eventService,
      MessageRepository messageRepository,
      PetalLayoutUtil petalLayoutUtil
  ) {
    this.eventService = eventService;
    this.messageRepository = messageRepository;
    this.petalLayoutUtil = petalLayoutUtil;
  }

  public UkaResponse getEventUka(String eventSlug) {
    Event event = eventService.findPublishedPlaypitEventBySlug(eventSlug);
    long totalMessages = messageRepository.countByEventIdAndStatus(
        event.getId(),
        MessageStatus.PUBLISHED
    );
    return new UkaResponse(
        event.getId(),
        event.getSlug(),
        totalMessages,
        petalLayoutUtil.calculateGrowthStage(totalMessages),
        messageRepository.findPublishedMessagesByEventId(
                event.getId(),
                MessageStatus.PUBLISHED,
                PageRequest.of(0, 300)
            )
            .stream()
            .map(PetalResponse::from)
            .toList()
    );
  }

  public UkaResponse eventUka(String eventSlug) {
    return getEventUka(eventSlug);
  }
}
