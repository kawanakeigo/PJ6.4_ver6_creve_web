package world.creve.platform.security;
import java.time.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
@Service public class LoginAttemptService {
    private final JdbcTemplate jdbc;
    private final IdentityHasher hasher;
    private final Clock clock;
    private final int max;
    private final int minutes;
    public LoginAttemptService(JdbcTemplate jdbc,IdentityHasher hasher,Clock clock,@Value("${creve.login.max-failures:5}")int max,@Value("${creve.login.lock-minutes:15}")int minutes) {
        this.jdbc=jdbc;
        this.hasher=hasher;
        this.clock=clock;
        this.max=Math.max(1,max);
        this.minutes=Math.max(1,minutes);
    }
    private String id(String name) {
        return hasher.hash("login:"+name.trim().toLowerCase(java.util.Locale.ROOT));
    }
    public boolean locked(String name) {
        return Boolean.TRUE.equals(jdbc.queryForObject("select exists(select 1 from login_attempts where identity_hash=? and locked_until>?)",Boolean.class,id(name),LocalDateTime.now(clock)));
    }
    @Transactional public void failure(String name) {
        LocalDateTime now=LocalDateTime.now(clock);
        String id=id(name);
        jdbc.update("insert into login_attempts(identity_hash,failures,window_start,updated_at) values(?,0,?,?) on conflict(identity_hash) do nothing",id,now,now);
        var rows=jdbc.queryForList("select failures,window_start from login_attempts where identity_hash=? for update",id);
        var row=rows.get(0);
        LocalDateTime start=((java.sql.Timestamp)row.get("window_start")).toLocalDateTime();
        int failures=start.isBefore(now.minusMinutes(minutes))?1:((Number)row.get("failures")).intValue()+1;
        if(start.isBefore(now.minusMinutes(minutes)))start=now;
        jdbc.update("update login_attempts set failures=?,window_start=?,locked_until=?,updated_at=? where identity_hash=?",failures,start,failures>=max?now.plusMinutes(minutes):null,now,id);
    }
    public void success(String name) {
        jdbc.update("delete from login_attempts where identity_hash=?",id(name));
    }
    @Scheduled(fixedDelay=600000) public void cleanup() {
        jdbc.update("delete from login_attempts where updated_at<? and (locked_until is null or locked_until<?)",LocalDateTime.now(clock).minusHours(1),LocalDateTime.now(clock));
    }
}
