package com.portfolio.edge.ratelimit;

/**
 * Per-client rate limit consumption (one permit per request when allowed).
 */
public interface RateLimitBucketStore {

    /**
     * @return {@code true} if the request is allowed under the limit for {@code clientKey}
     */
    boolean tryConsume(String clientKey);
}
