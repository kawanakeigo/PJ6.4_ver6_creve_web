package world.creve.nlj;
import java.util.List;
import org.springframework.stereotype.Component;
import world.creve.platform.spi.ProjectContribution;
import world.creve.platform.dto.EventResponse;
import world.creve.nlj.service.NljService;
@Component public class NljContribution implements ProjectContribution {
    private final NljService nlj;
    public NljContribution(NljService nlj) {
        this.nlj=nlj;
    }
    public int displayOrder() { return 20; }
    public String key() {
        return "NLJ";
    }
    public String title() {
        return "NLJ";
    }
    public String path() {
        return "/NLJ";
    }
    public List<EventResponse> upcomingEvents() {
        return nlj.getUpcomingNljEvents();
    }
}
