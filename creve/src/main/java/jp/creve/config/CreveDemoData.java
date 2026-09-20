package jp.creve.config;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(name = "creve.demo-data", havingValue = "true")
public class CreveDemoData implements ApplicationRunner {
    private final JdbcTemplate jdbc;
    private final PasswordEncoder passwords;
    private final Clock clock;

    public CreveDemoData(JdbcTemplate jdbc, PasswordEncoder passwords, Clock clock) {
        this.jdbc = jdbc;
        this.passwords = passwords;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Integer count = jdbc.queryForObject("select count(*) from official_content", Integer.class);
        if (count != null && count > 0) return;
        Instant now = clock.instant();
        content("ABOUT", "about", "CreVeについて", "CreVeは、つくる人の活動と、作品を受け取る人の体験をつなぐための基盤です。\n展示、ライブ、作者の部屋を、それぞれの事業として丁寧に育てます。", now);
        content("NEWS", "p0-open", "ローカルP0版を公開しました", "CreVe・PLAYPIT・NLJを独立したプロセスとDBに分け、設計確認用のローカル環境を用意しました。", now);
        content("CONTACT", "contact", "お問い合わせ", "お問い合わせ先: contact@creve.world\n受付時間: 平日 10:00-17:00", now);
        content("PRIVACY", "privacy", "プライバシーポリシー", "取得する情報、利用目的、安全管理、問い合わせ窓口を正式公開前に確定します。現在はローカル検証用の原稿です。", now);
        content("TERMS", "terms", "利用規約", "本サービスの利用条件を正式公開前に確定します。現在はローカル検証用の原稿です。", now);
        content("VR", "vr", "VR構想", "作品世界へ歩いて入れる空間を将来構想として検討しています。VR本体は今回のP0実装対象外です。", now);

        project("PLAYPIT", "PLAYPIT", "http://localhost:8081/playpit", 1, now);
        project("NLJ", "NEW LEGACY JAM", "http://localhost:8082/index.html", 2, now);

        UUID chaser = UUID.randomUUID();
        jdbc.update("""
                insert into app_user(user_id,email,password_hash,display_name,email_verified,status,created_at,updated_at)
                values(?,?,?,?,true,'ACTIVE',?,?)
                """, chaser, "chaser@creve.local", passwords.encode("creve-local-chaser"), "Local Chaser", db(now), db(now));
        jdbc.update("insert into app_user_role(user_id,role) values(?,'MEMBER'),(?,'CHASER')", chaser, chaser);

        UUID creatorId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        UUID publicationId = UUID.randomUUID();
        UUID workId = UUID.randomUUID();
        jdbc.update("insert into creator(creator_id,name,profile,status,created_at,updated_at) values(?,?,?,'ACTIVE',?,?)",
                creatorId, "水野 碧", "光と記憶の境界を描くビジュアルアーティスト。", db(now), db(now));
        jdbc.update("insert into creator_member(creator_id,user_id,role,created_at) values(?,?,'OWNER',?)", creatorId, chaser, db(now));
        jdbc.update("insert into room(room_id,creator_id,slug,state,created_at,updated_at) values(?,?,'aoi-light-room','PUBLIC',?,?)",
                roomId, creatorId, db(now), db(now));
        String snapshot = """
                {"schemaVersion":1,
                 "creator":{"id":"%s","name":"水野 碧","profile":"光と記憶の境界を描くビジュアルアーティスト。"},
                 "room":{"id":"%s","slug":"aoi-light-room","heading":"光が記憶になる部屋","intro":"残光や反射を手がかりに、見たあとにも残る景色を集めています。","theme":"DARK","links":[]},
                 "heroWorkId":"%s",
                 "works":[{"id":"%s","kind":"IMAGE","title":"残光","description":"時間の層を透過光で重ねた作品。","credit":"2026 / 水野 碧","assetId":null,"stories":[]}]}
                """.formatted(creatorId, roomId, workId, workId);
        jdbc.update("""
                insert into room_publication(publication_id,room_id,revision,schema_version,snapshot,status,
                  submitted_by,reviewed_by,submitted_at,reviewed_at,version)
                values(?,?,1,1,cast(? as jsonb),'APPROVED',?,?,?,?,0)
                """, publicationId, roomId, snapshot, chaser, chaser, db(now), db(now));
        jdbc.update("update room set current_publication_id=?,version=version+1,updated_at=? where room_id=?",
                publicationId, db(now), roomId);
        jdbc.update("insert into admin_guard(singleton_id,version) values(1,0)");
    }

    private void content(String type, String slug, String title, String body, Instant now) {
        jdbc.update("""
                insert into official_content(content_id,slug,type,title,body,published,created_at,updated_at)
                values(?,?,?,?,?,true,?,?)
                """, UUID.randomUUID(), slug, type, title, body, db(now), db(now));
    }

    private void project(String key, String label, String url, int position, Instant now) {
        jdbc.update("""
                insert into project_link(link_id,project_key,label,url,position,enabled,created_at,updated_at)
                values(?,?,?,?,?,true,?,?)
                """, UUID.randomUUID(), key, label, url, position, db(now), db(now));
    }

    private OffsetDateTime db(Instant instant) {
        return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
    }
}
