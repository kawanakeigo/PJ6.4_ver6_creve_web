package world.creve.playpit;
import org.springframework.stereotype.Component;
import java.util.List;
import world.creve.platform.spi.ProjectContribution;
import world.creve.platform.dto.EventResponse;
import world.creve.platform.service.EventService;
@Component public class PlaypitContribution implements ProjectContribution {
    private final EventService events;
    public PlaypitContribution(EventService events) {
        this.events=events;
    }
    public int displayOrder() { return 10; }
    public String key() {
        return "PLAYPIT";
    }
    public String title() {
        return "PLAYPIT";
    }
    public String path() {
        return "/playpit";
    }
    public List<EventResponse> upcomingEvents() {
        return events.getUpcomingPlaypitEvents();
    }
}
