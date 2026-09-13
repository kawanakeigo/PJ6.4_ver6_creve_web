package world.creve.platform.spi;
import java.util.List;
import world.creve.platform.dto.NewsResponse;
public interface NewsSource {
    List<NewsResponse> latestNews();
}
