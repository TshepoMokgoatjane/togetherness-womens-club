package za.co.twc.togetherness.womens.club.filter;

import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import za.co.twc.togetherness.womens.club.service.RateLimiterService;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RateLimitingFilter")
class RateLimitingFilterTest {

    @Mock
    private RateLimiterService rateLimiterService;

    @Mock
    private FilterChain filterChain;

    @Mock
    private Bucket bucket;

    @InjectMocks
    private RateLimitingFilter rateLimitingFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Nested
    @DisplayName("Login rate limiting")
    class LoginRateLimiting {

        @Test
        @DisplayName("should allow login POST when under rate limit")
        void shouldAllowLoginWhenUnderLimit() throws Exception {
            request.setMethod("POST");
            request.setRequestURI("/login");
            request.setRemoteAddr("192.168.1.1");

            when(rateLimiterService.resolveBucket(eq("login_192.168.1.1"), eq(5), eq(5), any(Duration.class)))
                    .thenReturn(bucket);
            when(bucket.tryConsume(1)).thenReturn(true);

            rateLimitingFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("should redirect to /login?rateLimited when login rate limit exceeded")
        void shouldRedirectWhenLoginLimitExceeded() throws Exception {
            request.setMethod("POST");
            request.setRequestURI("/login");
            request.setRemoteAddr("192.168.1.1");

            when(rateLimiterService.resolveBucket(eq("login_192.168.1.1"), eq(5), eq(5), any(Duration.class)))
                    .thenReturn(bucket);
            when(bucket.tryConsume(1)).thenReturn(false);

            rateLimitingFilter.doFilterInternal(request, response, filterChain);

            assertThat(response.getRedirectedUrl()).isEqualTo("/login?rateLimited");
            verify(filterChain, never()).doFilter(any(), any());
        }

        @Test
        @DisplayName("should use X-Forwarded-For header for IP when present")
        void shouldUseXForwardedForHeader() throws Exception {
            request.setMethod("POST");
            request.setRequestURI("/login");
            request.setRemoteAddr("10.0.0.1");
            request.addHeader("X-Forwarded-For", "203.0.113.50, 70.41.3.18");

            when(rateLimiterService.resolveBucket(eq("login_203.0.113.50"), eq(5), eq(5), any(Duration.class)))
                    .thenReturn(bucket);
            when(bucket.tryConsume(1)).thenReturn(true);

            rateLimitingFilter.doFilterInternal(request, response, filterChain);

            verify(rateLimiterService).resolveBucket(eq("login_203.0.113.50"), eq(5), eq(5), any(Duration.class));
            verify(filterChain).doFilter(request, response);
        }
    }

    @Nested
    @DisplayName("Registration rate limiting")
    class RegistrationRateLimiting {

        @Test
        @DisplayName("should allow registration POST when under rate limit")
        void shouldAllowRegistrationWhenUnderLimit() throws Exception {
            request.setMethod("POST");
            request.setRequestURI("/register");
            request.setRemoteAddr("192.168.1.1");

            when(rateLimiterService.resolveBucket(eq("register_192.168.1.1"), eq(3), eq(3), any(Duration.class)))
                    .thenReturn(bucket);
            when(bucket.tryConsume(1)).thenReturn(true);

            rateLimitingFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("should redirect when registration rate limit exceeded")
        void shouldRedirectWhenRegistrationLimitExceeded() throws Exception {
            request.setMethod("POST");
            request.setRequestURI("/register");
            request.setRemoteAddr("192.168.1.1");

            when(rateLimiterService.resolveBucket(eq("register_192.168.1.1"), eq(3), eq(3), any(Duration.class)))
                    .thenReturn(bucket);
            when(bucket.tryConsume(1)).thenReturn(false);

            rateLimitingFilter.doFilterInternal(request, response, filterChain);

            assertThat(response.getRedirectedUrl()).isEqualTo("/login?rateLimited");
            verify(filterChain, never()).doFilter(any(), any());
        }
    }

    @Nested
    @DisplayName("Forgot password rate limiting")
    class ForgotPasswordRateLimiting {

        @Test
        @DisplayName("should allow forgot-password POST when under rate limit")
        void shouldAllowForgotPasswordWhenUnderLimit() throws Exception {
            request.setMethod("POST");
            request.setRequestURI("/forgot-password");
            request.setRemoteAddr("192.168.1.1");

            when(rateLimiterService.resolveBucket(eq("forgot_192.168.1.1"), eq(3), eq(3), any(Duration.class)))
                    .thenReturn(bucket);
            when(bucket.tryConsume(1)).thenReturn(true);

            rateLimitingFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("should redirect when forgot-password rate limit exceeded")
        void shouldRedirectWhenForgotPasswordLimitExceeded() throws Exception {
            request.setMethod("POST");
            request.setRequestURI("/forgot-password");
            request.setRemoteAddr("192.168.1.1");

            when(rateLimiterService.resolveBucket(eq("forgot_192.168.1.1"), eq(3), eq(3), any(Duration.class)))
                    .thenReturn(bucket);
            when(bucket.tryConsume(1)).thenReturn(false);

            rateLimitingFilter.doFilterInternal(request, response, filterChain);

            assertThat(response.getRedirectedUrl()).isEqualTo("/login?rateLimited");
            verify(filterChain, never()).doFilter(any(), any());
        }
    }

    @Nested
    @DisplayName("Non-rate-limited requests")
    class NonRateLimitedRequests {

        @Test
        @DisplayName("should not rate limit GET requests")
        void shouldNotRateLimitGetRequests() throws Exception {
            request.setMethod("GET");
            request.setRequestURI("/login");

            rateLimitingFilter.doFilterInternal(request, response, filterChain);

            verify(rateLimiterService, never()).resolveBucket(any(), anyInt(), anyInt(), any());
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("should not rate limit POST to non-sensitive endpoints")
        void shouldNotRateLimitNonSensitivePost() throws Exception {
            request.setMethod("POST");
            request.setRequestURI("/members");

            rateLimitingFilter.doFilterInternal(request, response, filterChain);

            verify(rateLimiterService, never()).resolveBucket(any(), anyInt(), anyInt(), any());
            verify(filterChain).doFilter(request, response);
        }
    }
}
