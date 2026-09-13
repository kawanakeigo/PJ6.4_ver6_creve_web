package world.creve.portal.service;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.*;
import org.springframework.transaction.annotation.Transactional;
import world.creve.portal.repository.NewsRepository;
import world.creve.platform.dto.NewsResponse;
import world.creve.platform.spi.NewsSource;
@Service @Transactional(readOnly=true) public class NewsService implements NewsSource {
    private final NewsRepository news;
    public NewsService(NewsRepository news) {
        this.news=news;
    }
    public List<NewsResponse> latestNews() {
        return list(0,3).getContent();
    }
    public Page<NewsResponse> list(int page,int size) {
        return news.findByStatusOrderByPublishedAtDesc("PUBLISHED",PageRequest.of(Math.max(0,page),Math.min(30,Math.max(1,size)))).map(n->new NewsResponse(n.getNewsId(),n.getSlug(),n.getTitle(),n.getBody(),n.getPublishedAt()));
    }
}
