package world.creve.nlj.service;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import world.creve.platform.service.EventService;
import world.creve.platform.dto.*;
@Service public class NljService {
    private final EventService events;
    public NljService(EventService events) {
        this.events=events;
    }
    public List<EventResponse> getUpcomingNljEvents() {
        return events.upcoming("NLJ");
    }
    public Page<EventResponse> list(int page) {
        return events.list("NLJ",page);
    }
    public EventDetailResponse detail(String slug) {
        return events.getPublishedBySlug(slug,"NLJ");
    }
}
