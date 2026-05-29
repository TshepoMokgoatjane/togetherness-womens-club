package za.co.twc.togetherness.womens.club.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import za.co.twc.togetherness.womens.club.filter.RateLimitingFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, RateLimitingFilter rateLimitingFilter) throws Exception {
        http
                .addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/about", "/gallery", "/contact", "/login", "/register", "/forgot-password/**", "/reset-password/**", "/css/**", "/js/**", "/images/**", "/robots.txt", "/sitemap.xml").permitAll()
                        .requestMatchers("/profile/**", "/home", "/my/**", "/documents/**").hasAnyRole("ADMIN", "TREASURER", "USER")
                        .requestMatchers("/members/**", "/contributions/**", "/reports/**", "/claims/**").hasAnyRole("ADMIN", "TREASURER")
                        .requestMatchers("/admin/**").hasRole("ADMIN")
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
                        .cacheControl(cache -> cache.disable())
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("default-src 'self'; style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net; font-src 'self' https://cdn.jsdelivr.net; script-src 'self' 'unsafe-inline' 'unsafe-eval' https://cdn.jsdelivr.net; img-src 'self' data:; connect-src 'self' https://cdn.jsdelivr.net;")
                        )
                )
                .sessionManagement(session -> session
                        .sessionFixation().migrateSession()
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                        .expiredUrl("/login?expired")
                )
                .rememberMe(remember -> remember
                        .key("secure-remember-me-key")
                        .tokenValiditySeconds(7 * 24 * 60 * 60)
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
