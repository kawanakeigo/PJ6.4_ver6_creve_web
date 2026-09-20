package jp.creve.auth;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {
    private final JdbcTemplate jdbc;

    public DatabaseUserDetailsService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        String email = username == null ? "" : username.strip().toLowerCase(java.util.Locale.ROOT);
        var rows = jdbc.query("""
                select user_id,email,password_hash,status,email_verified from app_user where lower(email)=?
                """, (rs, row) -> new Account(
                rs.getObject("user_id", java.util.UUID.class), rs.getString("email"),
                rs.getString("password_hash"), rs.getString("status"), rs.getBoolean("email_verified")), email);
        if (rows.size() != 1) throw new UsernameNotFoundException("account not found");
        Account account = rows.getFirst();
        List<SimpleGrantedAuthority> authorities = jdbc.query("""
                select role from app_user_role where user_id=? order by role
                """, (rs, row) -> new SimpleGrantedAuthority("ROLE_" + rs.getString("role")), account.id());
        return User.withUsername(account.email()).password(account.password())
                .authorities(authorities).disabled(!"ACTIVE".equals(account.status()))
                .credentialsExpired(false).accountExpired(false).accountLocked(false)
                .build();
    }

    private record Account(java.util.UUID id, String email, String password, String status, boolean verified) {
    }
}
