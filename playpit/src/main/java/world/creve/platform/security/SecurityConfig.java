package world.creve.platform.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import world.creve.common.web.ApiError;

@Configuration
public class SecurityConfig {
    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http, LoginAttemptService attempts, ObjectMapper objectMapper) throws Exception {
        http.authorizeHttpRequests(authorize -> authorize
                .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                .requestMatchers("/admin/login", "/platform/**", "/creve/**", "/creators/**",
                        "/playpit/**", "/PLAYPIT/**", "/NLJ/**", "/live/**", "/lp/**").permitAll()
                .requestMatchers("/admin/**", "/api/admin/**").hasRole("ADMIN")
                .anyRequest().permitAll());
        http.formLogin(form -> form
                .loginPage("/admin/login")
                .loginProcessingUrl("/admin/login")
                .usernameParameter("email")
                .successHandler((request, response, authentication) -> {
                    attempts.success(authentication.getName());
                    log.info("admin_login_success");
                    response.sendRedirect(request.getContextPath() + "/admin/dashboard");
                })
                .failureHandler((request, response, exception) -> {
                    String email = request.getParameter("email");
                    if (email != null && email.length() <= 254) {
                        attempts.failure(email);
                    }
                    log.info("admin_login_failure");
                    response.sendRedirect(request.getContextPath() + "/admin/login?error");
                })
                .permitAll());
        http.logout(logout -> logout.logoutUrl("/admin/logout").logoutSuccessUrl("/admin/login?logout"));
        http.exceptionHandling(errors -> errors
                .authenticationEntryPoint((request, response, exception) -> {
                    if (request.getRequestURI().startsWith("/api/")) {
                        writeApiError(response, objectMapper, 401, "UNAUTHORIZED", "ログインが必要です。");
                    } else {
                        response.sendRedirect(request.getContextPath() + "/admin/login");
                    }
                })
                .accessDeniedHandler((request, response, exception) ->
                        writeApiError(response, objectMapper, 403, "FORBIDDEN", "操作が許可されていません。")));
        http.sessionManagement(session -> session.sessionFixation(fixation -> fixation.changeSessionId()));
        http.headers(headers -> headers.contentSecurityPolicy(policy -> policy.policyDirectives(
                "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; " +
                        "img-src 'self' https: http:; media-src 'self' https: http:; connect-src 'self'; " +
                        "object-src 'none'; base-uri 'self'; frame-ancestors 'none'; form-action 'self'")));
        return http.build();
    }

    private static void writeApiError(
            HttpServletResponse response, ObjectMapper objectMapper, int status, String code, String message)
            throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(java.nio.charset.StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getWriter(),
                new ApiError(code, message, List.of(), UUID.randomUUID().toString()));
    }
}
