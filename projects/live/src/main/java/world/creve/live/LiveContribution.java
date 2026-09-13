package world.creve.live;
import java.util.List;
import org.springframework.stereotype.Component;
import world.creve.platform.spi.ProjectContribution;
import world.creve.platform.dto.EventResponse;
import world.creve.live.service.LiveService;
@Component public class LiveContribution implements ProjectContribution {
    private final LiveService live;
    public LiveContribution(LiveService live) {
        this.live=live;
    }
    public int displayOrder() { return 20; }
    public String key() {
        return "LIVE";
    }
    public String title() {
        return "ライブ";
    }
    public String path() {
        return "/live";
    }
    public List<EventResponse> upcomingEvents() {
        return live.getUpcomingLiveEvents();
    }
}
