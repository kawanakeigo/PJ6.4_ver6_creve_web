package jp.creve.site.service;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import jp.creve.site.dao.SiteDao;
import jp.creve.site.model.PublicRoom;
import org.junit.jupiter.api.Test;

class PublicRoomQueryServiceTest {
    private final StubSiteDao dao = new StubSiteDao();
    private final PublicRoomQueryService service = new PublicRoomQueryService(dao);

    @Test
    void rejectsInvalidSlugBeforeQuery() {
        assertThrows(PublicRoomQueryService.RoomNotFoundException.class,
                () -> service.getBySlug("../private"));
        assertEquals(0, dao.queryCount);
    }

    @Test
    void hidesMissingOrNonPublicRoom() {
        assertThrows(PublicRoomQueryService.RoomNotFoundException.class,
                () -> service.getBySlug("hidden-room"));
    }

    @Test
    void returnsApprovedSnapshotProjectionFromDao() {
        var room = new PublicRoom(UUID.randomUUID(), UUID.randomUUID(), "approved-room",
                "作者", "紹介", "見出し", "入口文", "LIGHT", Instant.parse("2026-09-20T00:00:00Z"),
                List.of());
        dao.result = Optional.of(room);
        assertSame(room, service.getBySlug("approved-room"));
    }

    private static final class StubSiteDao extends SiteDao {
        private Optional<PublicRoom> result = Optional.empty();
        private int queryCount;

        private StubSiteDao() {
            super(null);
        }

        @Override
        public Optional<PublicRoom> room(String slug) {
            queryCount++;
            return result;
        }
    }
}
