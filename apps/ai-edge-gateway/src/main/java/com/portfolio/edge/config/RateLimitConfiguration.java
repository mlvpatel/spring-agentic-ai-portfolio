package com.portfolio.edge.config;

import com.portfolio.edge.ratelimit.InMemoryRateLimitBucketStore;
import com.portfolio.edge.ratelimit.RateLimitBucketStore;
import com.portfolio.edge.ratelimit.RedisRateLimitBucketStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
public class RateLimitConfiguration {

    @Bean
    RateLimitBucketStore rateLimitBucketStore(
            @Value("${gateway.rateLimiter.redisUrl:}") String redisUrl,
            @Value("${gateway.rateLimiter.replenishRate:100}") int replenishRate,
            @Value("${gateway.rateLimiter.burstCapacity:200}") int burstCapacity) {
        if (StringUtils.hasText(redisUrl)) {
            return new RedisRateLimitBucketStore(redisUrl.trim(), burstCapacity);
        }
        return new InMemoryRateLimitBucketStore(replenishRate, burstCapacity);
    }
}
