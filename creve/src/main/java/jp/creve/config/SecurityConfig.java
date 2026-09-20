package jp.creve.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    PasswordEncoder passwordEncoder() {
        return Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/about", "/news", "/contact", "/privacy", "/terms", "/vr",
                                "/discover", "/r/**", "/css/**", "/js/**", "/images/**", "/login").permitAll()
                        .requestMatchers("/admin/**", "/api/admin/**").hasRole("CHASER")
                        .requestMatchers("/studio/**", "/api/studio/**", "/account/**").authenticated()
                        .anyRequest().denyAll())
                .formLogin(form -> form.loginPage("/login").defaultSuccessUrl("/account/portal", true).permitAll())
                .logout(logout -> logout.logoutSuccessUrl("/"))
                .build();
    }
}
