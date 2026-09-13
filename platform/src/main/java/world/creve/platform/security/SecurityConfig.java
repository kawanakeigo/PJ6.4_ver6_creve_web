package world.creve.platform.security;
import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Configuration public class SecurityConfig {
    private static final Logger log=LoggerFactory.getLogger(SecurityConfig.class);
    @Bean public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
    @Bean public SecurityFilterChain filterChain(HttpSecurity http,LoginAttemptService attempts)throws Exception {
        http.authorizeHttpRequests(a->a.dispatcherTypeMatchers(DispatcherType.ERROR).permitAll() .requestMatchers("/admin/login","/platform/**","/creve/**","/creators/**","/playpit/**","/PLAYPIT/**","/NLJ/**","/live/**","/lp/**").permitAll() .requestMatchers("/admin/**","/api/admin/**").hasRole("ADMIN").anyRequest().permitAll());
        http.formLogin(f->f.loginPage("/admin/login").loginProcessingUrl("/admin/login").usernameParameter("email") .successHandler((req,res,auth)-> {
            attempts.success(auth.getName());log.info("admin_login_success");res.sendRedirect(req.getContextPath()+"/admin/dashboard");
        }
        ) .failureHandler((req,res,ex)-> {
            String email=req.getParameter("email");if(email!=null&&email.length()<=254)attempts.failure(email);log.info("admin_login_failure");res.sendRedirect(req.getContextPath()+"/admin/login?error");
        }
        ).permitAll());
        http.logout(l->l.logoutUrl("/admin/logout").logoutSuccessUrl("/admin/login?logout"));
        http.exceptionHandling(e->e.authenticationEntryPoint((req,res,ex)-> {
            if(req.getRequestURI().startsWith("/api/")) {
                res.setStatus(401);res.setContentType("application/json;charset=UTF-8");res.getWriter().write("{\"status\":401,\"error\":\"ログインが必要です。\"}");
            }
            else res.sendRedirect(req.getContextPath()+"/admin/login");
        }
        ) .accessDeniedHandler((req,res,ex)-> {
            res.setStatus(403);res.setContentType("application/json;charset=UTF-8");res.getWriter().write("{\"status\":403,\"error\":\"操作が許可されていません。\"}");
        }
        ));
        // Default session CSRF protection stays enabled; JavaScript sends the rendered masked token.
        http.sessionManagement(s->s.sessionFixation(f->f.changeSessionId()));
        http.headers(h->h.contentSecurityPolicy(c->c.policyDirectives("default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; img-src 'self' https: http:; media-src 'self' https: http:; connect-src 'self'; object-src 'none'; base-uri 'self'; frame-ancestors 'none'; form-action 'self'")));
        return http.build();
    }
}
