package  gateway.limiter;

public interface RateLimiter {
    boolean allow(String key);
}