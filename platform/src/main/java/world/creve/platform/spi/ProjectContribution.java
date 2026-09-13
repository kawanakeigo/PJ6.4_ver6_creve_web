package world.creve.platform.spi;
import java.util.List;
import world.creve.platform.dto.EventResponse;
public interface ProjectContribution {
    String key();
    String title();
    String path();
    default int displayOrder() { return 100; }
    default List<EventResponse> upcomingEvents() {
        return List.of();
    }
}
