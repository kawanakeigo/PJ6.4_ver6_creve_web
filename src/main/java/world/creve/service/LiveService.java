package world.creve.service;

import java.util.List;
import org.springframework.stereotype.Service;
import world.creve.entity.Event;

@Service
public class LiveService {
  private final EventService eventService;

  public LiveService(EventService eventService) {
    this.eventService = eventService;
  }

  public List<Event> getPublishedLiveEvents() {
    return eventService.getPublishedLiveEvents();
  }

  public Event getPublishedLiveEvent(String eventSlug) {
    return eventService.findPublishedLiveEventBySlug(eventSlug);
  }
}
