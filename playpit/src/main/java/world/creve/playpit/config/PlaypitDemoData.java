package world.creve.playpit.config;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(name = "playpit.demo-data", havingValue = "true")
public class PlaypitDemoData implements ApplicationRunner {
    private final JdbcTemplate jdbc;
    private final Clock clock;

    public PlaypitDemoData(JdbcTemplate jdbc, Clock clock) {
        this.jdbc = jdbc;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Integer count = jdbc.queryForObject("select count(*) from events", Integer.class);
        if (count != null && count > 0) return;

        LocalDateTime start = LocalDateTime.ofInstant(clock.instant(), ZoneId.of("Asia/Tokyo"))
                .plusDays(10).withHour(11).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = start.plusDays(1).withHour(18);
        Long eventId = jdbc.queryForObject("""
                insert into events(event_type,slug,title,summary,description,main_image_url,start_at,end_at,
                  venue_name,address,access,price,status,accepting_messages)
                values('PLAYPIT','playpit-2026','PLAYPIT 2026','つくる人と、受け取る人が出会う展示イベント',
                  '作品を見て、感じたことばをその場で作者へ届けられるPLAYPITの公開展示です。',
                  '/playpit/images/brand/playpit-event-2026.png',?,?,
                  'CreVe Studio','東京都渋谷区','渋谷駅から徒歩8分','入場無料','PUBLISHED',true)
                returning event_id
                """, Long.class, Timestamp.valueOf(start), Timestamp.valueOf(end));

        Long creator1 = creator("aoi-mizuno", "水野 碧", "ミズノ アオイ", "光と記憶の境界を描くビジュアルアーティスト。", "光の余白", "映像・インスタレーション", "/playpit/images/creators/creator-001/icon.svg");
        Long creator2 = creator("ren-kisaragi", "如月 蓮", "キサラギ レン", "街の音を素材に、体験できる音の風景をつくります。", "聴こえない風景", "サウンドアート", "/playpit/images/creators/creator-002/icon.svg");
        Long creator3 = creator("mio-tsukishima", "月島 澪", "ツキシマ ミオ", "紙と身体の動きから生まれる造形を探っています。", "触れる時間", "彫刻・紙", "/playpit/images/creators/creator-003/icon.svg");

        Long artwork1 = artwork(creator1, "afterimage", "残光", "時間の層を透過光で重ねる映像作品。", "アクリル・プロジェクション", "/playpit/images/creators/creator-001/main.svg");
        Long artwork2 = artwork(creator2, "city-breath", "街の呼吸", "会場周辺で集めた音から構成するサウンド作品。", "フィールドレコーディング", "/playpit/images/creators/creator-002/main.svg");
        Long artwork3 = artwork(creator3, "folded-time", "折られた時間", "一枚の紙が空間へ変わる瞬間を留めた作品。", "和紙・糸", "/playpit/images/creators/creator-003/main.svg");

        participant(eventId, creator1, 1, "光の展示");
        participant(eventId, creator2, 2, "音の展示");
        participant(eventId, creator3, 3, "紙の展示");
        exhibition(eventId, artwork1, 1, "Gallery A");
        exhibition(eventId, artwork2, 2, "Listening Room");
        exhibition(eventId, artwork3, 3, "Gallery B");

        message(eventId, creator1, artwork1, "光が呼吸しているようで、ずっと見ていたくなりました。", "来場者A");
        message(eventId, creator2, artwork2, "いつもの街に、知らないリズムが隠れていたことに気づきました。", null);
        message(eventId, creator3, artwork3, "紙の影まで作品の一部に見えて、とても静かな気持ちになりました。", "mugi");
    }

    private Long creator(String slug, String name, String kana, String profile, String concept, String genre, String image) {
        return jdbc.queryForObject("""
                insert into creators(slug,name,name_kana,profile,concept,genre,profile_image_url,status)
                values(?,?,?,?,?,?,?,'PUBLISHED') returning creator_id
                """, Long.class, slug, name, kana, profile, concept, genre, image);
    }

    private Long artwork(Long creatorId, String slug, String title, String description, String materials, String image) {
        return jdbc.queryForObject("""
                insert into artworks(creator_id,slug,title,description,concept,materials,production_year,main_image_url,status)
                values(?,?,?,?,?,?,2026,?,'PUBLISHED') returning artwork_id
                """, Long.class, creatorId, slug, title, description, description, materials, image);
    }

    private void participant(Long eventId, Long creatorId, int order, String title) {
        jdbc.update("insert into event_creators(event_id,creator_id,exhibition_title,display_order) values(?,?,?,?)",
                eventId, creatorId, title, order);
    }

    private void exhibition(Long eventId, Long artworkId, int order, String area) {
        jdbc.update("insert into event_artworks(event_id,artwork_id,exhibition_area,display_order,qr_code_url) values(?,?,?,?,?)",
                eventId, artworkId, area, order, "/playpit/playpit-2026/artworks/" + artworkId + "/qr.png");
    }

    private void message(Long eventId, Long creatorId, Long artworkId, String body, String name) {
        jdbc.update("""
                insert into messages(event_id,creator_id,artwork_id,message,display_name,is_anonymous,agreement,status)
                values(?,?,?,?,?,? ,true,'PUBLISHED')
                """, eventId, creatorId, artworkId, body, name, name == null);
    }
}
