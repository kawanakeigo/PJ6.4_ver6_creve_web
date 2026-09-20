package jp.creve.site.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import jp.creve.site.model.OfficialContent;
import jp.creve.site.model.ProjectLink;
import jp.creve.site.model.PublicRoom;
import jp.creve.site.model.PublicRoomCard;
import jp.creve.site.model.PublicWork;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class SiteDao {
    private static final String PUBLIC_ROOM_WHERE = """
            c.status='ACTIVE' and r.state='PUBLIC' and p.status='APPROVED'
            and r.current_publication_id=p.publication_id and p.room_id=r.room_id
            and not exists (
              select 1 from publication_asset pa join asset a on a.asset_id=pa.asset_id
              where pa.publication_id=p.publication_id and a.state<>'READY'
            )
            """;

    private final JdbcTemplate jdbc;

    public SiteDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<OfficialContent> content(String type) {
        return jdbc.query("""
                select content_id,type,slug,title,body from official_content
                where type=? and published=true order by updated_at desc limit 1
                """, this::official, type).stream().findFirst();
    }

    public List<OfficialContent> news() {
        return jdbc.query("""
                select content_id,type,slug,title,body from official_content
                where type='NEWS' and published=true order by updated_at desc,content_id limit 12
                """, this::official);
    }

    public List<ProjectLink> links() {
        return jdbc.query("""
                select link_id,project_key,label,url,position from project_link
                where enabled=true order by position,project_key
                """, (rs, row) -> new ProjectLink(
                rs.getObject("link_id", UUID.class), rs.getString("project_key"),
                rs.getString("label"), rs.getString("url"), rs.getInt("position")));
    }

    public List<PublicRoomCard> rooms() {
        String sql = """
                select r.room_id,r.slug,c.name,p.reviewed_at,
                  p.snapshot->'room'->>'heading' heading,
                  p.snapshot->'room'->>'intro' intro,
                  p.snapshot->'room'->>'theme' theme
                from room r join creator c on c.creator_id=r.creator_id
                join room_publication p on p.publication_id=r.current_publication_id
                where %s order by p.reviewed_at desc,p.publication_id desc limit 30
                """.formatted(PUBLIC_ROOM_WHERE);
        return jdbc.query(sql, (rs, row) -> new PublicRoomCard(
                rs.getObject("room_id", UUID.class), rs.getString("slug"), rs.getString("name"),
                rs.getString("heading"), rs.getString("intro"), rs.getString("theme"),
                rs.getTimestamp("reviewed_at").toInstant()));
    }

    public Optional<PublicRoom> room(String slug) {
        String sql = """
                select r.room_id,r.slug,c.name,p.publication_id,p.reviewed_at,
                  p.snapshot->'creator'->>'profile' creator_profile,
                  p.snapshot->'room'->>'heading' heading,
                  p.snapshot->'room'->>'intro' intro,
                  p.snapshot->'room'->>'theme' theme
                from room r join creator c on c.creator_id=r.creator_id
                join room_publication p on p.publication_id=r.current_publication_id
                where r.slug=? and %s
                """.formatted(PUBLIC_ROOM_WHERE);
        return jdbc.query(sql, (rs, row) -> new PublicRoom(
                rs.getObject("room_id", UUID.class), rs.getObject("publication_id", UUID.class),
                rs.getString("slug"), rs.getString("name"), rs.getString("creator_profile"),
                rs.getString("heading"), rs.getString("intro"), rs.getString("theme"),
                rs.getTimestamp("reviewed_at").toInstant(), works(rs.getObject("publication_id", UUID.class))), slug)
                .stream().findFirst();
    }

    private List<PublicWork> works(UUID publicationId) {
        return jdbc.query("""
                select item->>'id' id,item->>'kind' kind,item->>'title' title,
                  item->>'description' description,item->>'credit' credit,ordinality
                from room_publication p,
                  jsonb_array_elements(p.snapshot->'works') with ordinality as work(item,ordinality)
                where p.publication_id=? order by ordinality
                """, (rs, row) -> new PublicWork(
                rs.getString("id"), rs.getString("kind"), rs.getString("title"),
                rs.getString("description"), rs.getString("credit"), rs.getInt("ordinality")), publicationId);
    }

    private OfficialContent official(ResultSet rs, int row) throws SQLException {
        return new OfficialContent(rs.getObject("content_id", UUID.class), rs.getString("type"),
                rs.getString("slug"), rs.getString("title"), rs.getString("body"));
    }
}
