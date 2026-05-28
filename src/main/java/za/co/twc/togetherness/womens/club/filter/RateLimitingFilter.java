package za.co.twc.togetherness.womens.club.filter;

import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import za.co.twc.togetherness.womens.club.service.RateLimiterService;
import za.co.twc.togetherness.womens.club.utilities.RequestUtils;

import java.io.IOException;
import java.time.Duration;

/**
 * Rate limits login and registration POST requests to prevent brute-force attacks.
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final RateLimiterService rateLimiterService;

    public RateLimitingFilter(RateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        // Only rate-limit POST requests to sensitive endpoints
        if ("POST".equalsIgnoreCase(method)) {
            String ip = RequestUtils.getClientIP(request);
            Bucket bucket = null;

            if ("/login".equals(path)) {
                // 5 login attempts per minute
                bucket = rateLimiterService.resolveBucket("login_" + ip, 5, 5, Duration.ofMinutes(1));
            } else if ("/register".equals(path)) {
                // 3 registration attempts per minute
                bucket = rateLimiterService.resolveBucket("register_" + ip, 3, 3, Duration.ofMinutes(1));
            } else if ("/forgot-password".equals(path)) {
                // 3 password reset requests per minute
                bucket = rateLimiterService.resolveBucket("forgot_" + ip, 3, 3, Duration.ofMinutes(1));
            }

            if (bucket != null && !bucket.tryConsume(1)) {
                response.sendRedirect("/login?rateLimited");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
