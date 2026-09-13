package world.creve.platform.service;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import world.creve.platform.repository.AdminRepository;
import world.creve.platform.entity.Admin;
import world.creve.platform.security.LoginAttemptService;
@Service public class AdminService implements UserDetailsService {
    private final AdminRepository admins;
    private final LoginAttemptService attempts;
    public AdminService(AdminRepository admins,LoginAttemptService attempts) {
        this.admins=admins;
        this.attempts=attempts;
    }
    @Override @Transactional(readOnly=true) public UserDetails loadUserByUsername(String username) {
        if(attempts.locked(username))throw new LockedException("認証は一時的に制限されています。");
        Admin a=admins.findByEmailIgnoreCase(username.trim()).filter(x->Boolean.TRUE.equals(x.getEnabled())).orElseThrow(()->new UsernameNotFoundException("認証に失敗しました。"));
        return User.withUsername(a.getEmail()).password(a.getPasswordHash()).roles("ADMIN").build();
    }
    @Transactional public void bootstrap(String email,String password,PasswordEncoder encoder) {
        if(email==null||!email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")||password==null||password.length()<12)throw new IllegalStateException("Explicit admin email and a password of at least 12 characters are required");
        if(admins.count()!=0)return;
        Admin a=new Admin();
        a.setEmail(email.trim().toLowerCase(java.util.Locale.ROOT));
        a.setPasswordHash(encoder.encode(password));
        a.setEnabled(true);
        admins.save(a);
    }
}
