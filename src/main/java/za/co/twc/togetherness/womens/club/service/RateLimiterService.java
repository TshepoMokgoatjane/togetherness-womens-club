package za.co.twc.togetherness.womens.club.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory rate limiter using Bucket4j.
 * Suitable for single-instance deployments (Railway single replica).
 * Swap to Redis-backed ProxyManager if scaling to multiple instances.
 */
@Service
public class RateLimiterService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RateLimiterService.class);

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    /**
     * Resolve or create a rate-limiting bucket for the given key.
     *
     * @param key          unique identifier (e.g., "login_192.168.1.1")
     * @param capacity     max tokens in the bucket
     * @param refillTokens tokens added per interval
     * @param duration     refill interval
     */
    public Bucket resolveBucket(String key, int capacity, int refillTokens, Duration duration) {
        return buckets.computeIfAbsent(key, k -> {
            LOGGER.debug("Creating rate limit bucket for key: {}", k);
            return Bucket.builder()
                    .addLimit(Bandwidth.builder()
                            .capacity(capacity)
                            .refillIntervally(refillTokens, duration)
                            .build())
                    .build();
        });
    }

    /**
     * Default bucket: 10 requests per minute.
     */
    public Bucket resolveBucket(String key) {
        return resolveBucket(key, 10, 10, Duration.ofMinutes(1));
    }
}
