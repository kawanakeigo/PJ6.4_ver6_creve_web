package world.creve.live.service;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import world.creve.platform.service.EventService;
import world.creve.platform.dto.*;
@Service public class LiveService {
    private final EventService events;
    public LiveService(EventService events) {
        this.events=events;
    }
    public List<EventResponse> getUpcomingLiveEvents() {
        return events.upcoming("LIVE");
    }
    public Page<EventResponse> list(int page) {
        return events.list("LIVE",page);
    }
    public EventDetailResponse detail(String slug) {
        return events.getPublishedBySlug(slug,"LIVE");
    }
}
