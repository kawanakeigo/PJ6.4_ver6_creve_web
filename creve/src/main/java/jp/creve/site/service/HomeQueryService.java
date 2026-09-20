package jp.creve.site.service;

import java.util.List;
import jp.creve.site.dao.SiteDao;
import jp.creve.site.model.OfficialContent;
import jp.creve.site.model.ProjectLink;
import jp.creve.site.model.PublicRoomCard;
import org.springframework.stereotype.Service;

@Service
public class HomeQueryService {
    private final SiteDao dao;

    public HomeQueryService(SiteDao dao) {
        this.dao = dao;
    }

    public List<ProjectLink> projectLinks() {
        return dao.links();
    }

    public List<OfficialContent> news() {
        return dao.news();
    }

    public List<PublicRoomCard> rooms() {
        return dao.rooms();
    }

    public OfficialContent content(String type) {
        return dao.content(type).orElseThrow(() -> new ContentNotFoundException(type));
    }

    public static class ContentNotFoundException extends RuntimeException {
        public ContentNotFoundException(String type) {
            super(type);
        }
    }
}
