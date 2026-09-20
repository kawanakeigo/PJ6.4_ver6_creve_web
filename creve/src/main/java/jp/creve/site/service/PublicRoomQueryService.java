package jp.creve.site.service;

import jp.creve.site.dao.SiteDao;
import jp.creve.site.model.PublicRoom;
import org.springframework.stereotype.Service;

@Service
public class PublicRoomQueryService {
    private final SiteDao dao;

    public PublicRoomQueryService(SiteDao dao) {
        this.dao = dao;
    }

    public PublicRoom getBySlug(String slug) {
        if (slug == null || !slug.matches("[a-z0-9]+(?:-[a-z0-9]+)*")) throw new RoomNotFoundException();
        return dao.room(slug).orElseThrow(RoomNotFoundException::new);
    }

    public static class RoomNotFoundException extends RuntimeException {
    }
}
