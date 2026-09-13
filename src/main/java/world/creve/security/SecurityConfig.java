package world.creve.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import world.creve.repository.AdminRepository;

@Configuration
public class SecurityConfig {
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(authorize -> authorize
            .requestMatchers(
                "/admin/login",
                "/admin/logout",
                "/css/**",
                "/js/**",
                "/images/**",
                "/favicon.ico"
            ).permitAll()
            .requestMatchers("/admin/**", "/api/admin/**").hasRole("ADMIN")
            .anyRequest().permitAll())
        .formLogin(form -> form
            .loginPage("/admin/login")
            .loginProcessingUrl("/admin/login")
            .usernameParameter("email")
            .passwordParameter("password")
            .defaultSuccessUrl("/admin/messages", true)
            .failureUrl("/admin/login?error")
            .permitAll())
        .logout(logout -> logout
            .logoutUrl("/admin/logout")
            .logoutSuccessUrl("/admin/login?logout")
            .permitAll())
        .sessionManagement(session -> session
            .sessionFixation(fixation -> fixation.migrateSession()));
    return http.build();
  }

  @Bean
  public UserDetailsService userDetailsService(AdminRepository adminRepository) {
    return email -> adminRepository.findByEmail(email.trim().toLowerCase())
        .filter(admin -> admin.isEnabled())
        .map(admin -> User.withUsername(admin.getEmail())
            .password(admin.getPasswordHash())
            .roles("ADMIN")
            .build())
        .orElseThrow(() -> new UsernameNotFoundException("Admin not found."));
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
