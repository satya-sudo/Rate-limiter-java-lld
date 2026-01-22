package gateway.limiter;

public interface ManagedLimiter extends RateLimiter {
    void start();
    void stop();
}