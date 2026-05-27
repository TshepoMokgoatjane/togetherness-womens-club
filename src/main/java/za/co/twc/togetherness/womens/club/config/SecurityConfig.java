package za.co.twc.togetherness.womens.club.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/register", "/forgot-password/**", "/reset-password/**", "/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/profile/**", "/home", "/my/**").hasAnyRole("ADMIN", "TREASURER", "USER")
                        .requestMatchers("/members/**", "/contributions/**", "/reports/**", "/claims/**").hasAnyRole("ADMIN", "TREASURER")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/login-success", true)
                        .permitAll())
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll())
                .exceptionHandling(ex -> ex
                        .accessDeniedPage("/access-denied"))
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("default-src 'self'; style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net; font-src 'self' https://cdn.jsdelivr.net; script-src 'self' 'unsafe-inline' 'unsafe-eval' https://cdn.jsdelivr.net; img-src 'self' data:; connect-src 'self' https://cdn.jsdelivr.net;")
                        )
                )
                .sessionManagement(session -> session
                        .sessionFixation().migrateSession()
                        .maximumSessions(1) // Allow only 1 active session
                        .maxSessionsPreventsLogin(false) // Kicks the old session instead of blocking new login
                        .expiredUrl("/login?expired") // Redirect to login with message
                )
                .rememberMe(remember -> remember
                        .key("secure-remember-me-key")
                        .tokenValiditySeconds(7 * 24 * 60 * 60) // 7 days
                )
        ;

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
