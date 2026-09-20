package world.creve.platform.service;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.*;
import world.creve.platform.entity.*;
import world.creve.platform.repository.*;
import world.creve.platform.dto.*;
import world.creve.platform.spi.NewsSource;
import world.creve.platform.exception.*;
import world.creve.platform.util.PageBounds;
@Service @Transactional(readOnly=true) public class EventService {
    private final EventRepository events;
    private final Clock clock;
    private final List<NewsSource> newsSources;
    public EventService(EventRepository events,Clock clock,List<NewsSource> newsSources) {
        this.events=events;
        this.clock=clock;
        this.newsSources=new ArrayList<>(newsSources);
    }
    public Event requirePublished(Long eventId) {
        return events.findById(eventId).filter(e->"PUBLISHED".equals(e.getStatus())).orElseThrow(EventNotFoundException::new);
    }
    public EventDetailResponse getPublishedPlaypitEventBySlug(String slug) {
        return getPublishedBySlug(slug,"PLAYPIT");
    }
    public EventDetailResponse getPublishedBySlug(String slug,String type) {
        Event e=events.findBySlugAndStatus(slug,"PUBLISHED").filter(x->type.equals(x.getEventType())).orElseThrow(EventNotFoundException::new);
        return detail(e);
    }
    public List<EventResponse> getUpcomingPlaypitEvents() {
        return upcoming("PLAYPIT");
    }
    public List<EventResponse> getPublishedEventsByCreatorId(Long creatorId) {
        return events.findPublishedEventsByCreatorId(creatorId).stream().map(this::summary).toList();
    }
    public List<EventResponse> upcoming(String type) {
        return events.findUpcomingEvents(type,LocalDateTime.now(clock),PageRequest.of(0,3)).stream().map(this::summary).toList();
    }
    public List<NewsResponse> getLatestNews() {
        return newsSources.stream().flatMap(s->s.latestNews().stream()).sorted(Comparator.comparing(NewsResponse::publishedAt).reversed()).limit(3).toList();
    }
    public Page<EventResponse> list(String type,int page) {
        return events.findByEventTypeAndStatusOrderByStartAtDesc(type,"PUBLISHED",PageRequest.of(PageBounds.page(page),30)).map(this::summary);
    }
    public Page<EventResponse> archive(String type,int page) {
        return events.findPastEvents(type,LocalDateTime.now(clock),PageRequest.of(PageBounds.page(page),30)).map(this::summary);
    }
    public String phase(Event e) {
        LocalDateTime now=LocalDateTime.now(clock);
        return now.isBefore(e.getStartAt())?"開催予定":(now.isAfter(e.getEndAt())?"終了済み":"開催中");
    }
    public EventResponse summary(Event e) {
        return new EventResponse(e.getEventId(),e.getEventType(),e.getSlug(),e.getTitle(),e.getSummary(),e.getMainImageUrl(),e.getStartAt(),e.getEndAt(),e.getVenueName(),phase(e));
    }
    public EventDetailResponse detail(Event e) {
        return new EventDetailResponse(e.getEventId(),e.getEventType(),e.getSlug(),e.getTitle(),e.getSummary(),e.getDescription(),e.getMainImageUrl(),e.getStartAt(),e.getEndAt(),e.getVenueName(),e.getAddress(),e.getAccess(),e.getPrice(),e.getTicketUrl(),phase(e));
    }
}
