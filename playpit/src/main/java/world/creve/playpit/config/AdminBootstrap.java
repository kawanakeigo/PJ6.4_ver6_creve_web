package world.creve.playpit.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import world.creve.platform.service.AdminService;

@Component
public class AdminBootstrap implements CommandLineRunner {
    private final AdminService admins;
    private final PasswordEncoder encoder;
    private final boolean enabled;
    private final String email;
    private final String password;

    public AdminBootstrap(
            AdminService admins,
            PasswordEncoder encoder,
            @Value("${playpit.bootstrap-admin:false}") boolean enabled,
            @Value("${playpit.admin.email:}") String email,
            @Value("${playpit.admin.password:}") String password) {
        this.admins = admins;
        this.encoder = encoder;
        this.enabled = enabled;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(String... args) {
        if (enabled) {
            admins.bootstrap(email, password, encoder);
        }
    }
}
