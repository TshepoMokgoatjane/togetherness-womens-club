package za.co.twc.togetherness.womens.club.service;

import io.github.bucket4j.Bucket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RateLimiterService")
class RateLimiterServiceTest {

    private RateLimiterService rateLimiterService;

    @BeforeEach
    void setUp() {
        rateLimiterService = new RateLimiterService();
    }

    @Test
    @DisplayName("should create bucket with specified capacity")
    void shouldCreateBucketWithCapacity() {
        Bucket bucket = rateLimiterService.resolveBucket("test_key", 5, 5, Duration.ofMinutes(1));

        // Should allow 5 requests
        for (int i = 0; i < 5; i++) {
            assertThat(bucket.tryConsume(1)).isTrue();
        }
        // 6th should be denied
        assertThat(bucket.tryConsume(1)).isFalse();
    }

    @Test
    @DisplayName("should return same bucket for same key")
    void shouldReturnSameBucketForSameKey() {
        Bucket bucket1 = rateLimiterService.resolveBucket("same_key", 10, 10, Duration.ofMinutes(1));
        Bucket bucket2 = rateLimiterService.resolveBucket("same_key", 10, 10, Duration.ofMinutes(1));

        assertThat(bucket1).isSameAs(bucket2);
    }

    @Test
    @DisplayName("should return different buckets for different keys")
    void shouldReturnDifferentBucketsForDifferentKeys() {
        Bucket bucket1 = rateLimiterService.resolveBucket("key_1", 10, 10, Duration.ofMinutes(1));
        Bucket bucket2 = rateLimiterService.resolveBucket("key_2", 10, 10, Duration.ofMinutes(1));

        assertThat(bucket1).isNotSameAs(bucket2);
    }

    @Test
    @DisplayName("should use default bucket with 10 requests per minute")
    void shouldUseDefaultBucket() {
        Bucket bucket = rateLimiterService.resolveBucket("default_key");

        // Should allow 10 requests
        for (int i = 0; i < 10; i++) {
            assertThat(bucket.tryConsume(1)).isTrue();
        }
        // 11th should be denied
        assertThat(bucket.tryConsume(1)).isFalse();
    }

    @Test
    @DisplayName("should enforce login rate limit of 5 per minute")
    void shouldEnforceLoginRateLimit() {
        Bucket bucket = rateLimiterService.resolveBucket("login_192.168.1.1", 5, 5, Duration.ofMinutes(1));

        for (int i = 0; i < 5; i++) {
            assertThat(bucket.tryConsume(1))
                    .as("Request %d should be allowed", i + 1)
                    .isTrue();
        }
        assertThat(bucket.tryConsume(1))
                .as("6th request should be denied")
                .isFalse();
    }

    @Test
    @DisplayName("should enforce registration rate limit of 3 per minute")
    void shouldEnforceRegistrationRateLimit() {
        Bucket bucket = rateLimiterService.resolveBucket("register_192.168.1.1", 3, 3, Duration.ofMinutes(1));

        for (int i = 0; i < 3; i++) {
            assertThat(bucket.tryConsume(1))
                    .as("Request %d should be allowed", i + 1)
                    .isTrue();
        }
        assertThat(bucket.tryConsume(1))
                .as("4th request should be denied")
                .isFalse();
    }
}
