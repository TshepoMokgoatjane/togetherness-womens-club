package za.co.twc.togetherness.womens.club.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.WebContentInterceptor;

import java.time.Duration;

/**
 * Configures caching for public HTML pages.
 * Spring Security sets no-cache by default on all responses.
 * This interceptor overrides that for public pages, allowing browsers
 * to cache them briefly and reduce load times on low bandwidth.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        WebContentInterceptor publicPageCache = new WebContentInterceptor();
        publicPageCache.setCacheControl(CacheControl.maxAge(Duration.ofMinutes(10)).cachePublic());

        registry.addInterceptor(publicPageCache)
                .addPathPatterns("/", "/about", "/gallery", "/contact");
    }
}
