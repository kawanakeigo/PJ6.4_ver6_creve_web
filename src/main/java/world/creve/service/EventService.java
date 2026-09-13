package world.creve.service;

import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import world.creve.entity.Event;
import world.creve.entity.EventType;
import world.creve.entity.News;
import world.creve.entity.PublishStatus;
import world.creve.exception.NotFoundException;
import world.creve.repository.EventRepository;
import world.creve.repository.NewsRepository;

@Service
public class EventService {
  private final EventRepository eventRepository;
  private final NewsRepository newsRepository;

  public EventService(EventRepository eventRepository, NewsRepository newsRepository) {
    this.eventRepository = eventRepository;
    this.newsRepository = newsRepository;
  }

  public List<Event> findAllForAdmin() {
    return eventRepository.findAll();
  }

  public Event findById(String eventId) {
    return eventRepository.findById(eventId)
        .orElseThrow(() -> new NotFoundException("イベントが見つかりません。"));
  }

  public Event findPublishedEventBySlug(String slug) {
    return eventRepository.findBySlugAndStatus(slug, PublishStatus.PUBLISHED)
        .or(() -> eventRepository.findById(slug).filter(Event::isPublished))
        .orElseThrow(() -> new NotFoundException("イベントが見つかりません。"));
  }

  public Event findPublishedPlaypitEventBySlug(String slug) {
    return eventRepository.findBySlugAndEventTypeAndStatus(
            slug,
            EventType.PLAYPIT,
            PublishStatus.PUBLISHED
        )
        .or(() -> eventRepository.findById(slug)
            .filter(Event::isPublished)
            .filter(event -> event.getEventType() == EventType.PLAYPIT))
        .orElseThrow(() -> new NotFoundException("PLAYPITイベントが見つかりません。"));
  }

  public Event findPublishedLiveEventBySlug(String slug) {
    return eventRepository.findBySlugAndEventTypeAndStatus(
            slug,
            EventType.LIVE,
            PublishStatus.PUBLISHED
        )
        .or(() -> eventRepository.findById(slug)
            .filter(Event::isPublished)
            .filter(event -> event.getEventType() == EventType.LIVE))
        .orElseThrow(() -> new NotFoundException("ライブイベントが見つかりません。"));
  }

  public Event findDefaultPlaypitEvent() {
    return getPublishedPlaypitEvents().stream()
        .findFirst()
        .orElseThrow(() -> new NotFoundException("公開中のPLAYPITイベントが見つかりません。"));
  }

  public List<Event> getPublishedPlaypitEvents() {
    return eventRepository.findByEventTypeAndStatusOrderByStartAtDesc(
        EventType.PLAYPIT,
        PublishStatus.PUBLISHED
    );
  }

  public List<Event> getPublishedLiveEvents() {
    return eventRepository.findByEventTypeAndStatusOrderByStartAtDesc(
        EventType.LIVE,
        PublishStatus.PUBLISHED
    );
  }

  public List<Event> getUpcomingEvents() {
    return eventRepository.findUpcomingEvents(PublishStatus.PUBLISHED, OffsetDateTime.now());
  }

  public List<Event> getUpcomingPlaypitEvents() {
    return getUpcomingEvents().stream()
        .filter(event -> event.getEventType() == EventType.PLAYPIT)
        .toList();
  }

  public List<Event> getUpcomingLiveEvents() {
    return getUpcomingEvents().stream()
        .filter(event -> event.getEventType() == EventType.LIVE)
        .toList();
  }

  public List<News> getLatestNews() {
    return newsRepository.findTop3ByStatusOrderByPublishedAtDesc(PublishStatus.PUBLISHED);
  }

  public List<News> getPublishedNews() {
    return newsRepository.findByStatusOrderByPublishedAtDesc(PublishStatus.PUBLISHED);
  }
}
