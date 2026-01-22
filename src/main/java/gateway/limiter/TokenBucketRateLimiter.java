package gateway.limiter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TokenBucketRateLimiter  implements ManagedLimiter{
    private final double capacity;
    private final double refillRate;
    private final ConcurrentHashMap<String , TokenBucket> tokenBuckets =  new ConcurrentHashMap<>();
    private final long idleThresholdMillis;
    private final long cleanupIntervalMillis;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public TokenBucketRateLimiter(double capacity, double refillRate, long idleThresholdMillis, long cleanupIntervalMillis ){
        this.capacity = capacity;
        this.refillRate = refillRate;
        this.cleanupIntervalMillis = cleanupIntervalMillis;
        this.idleThresholdMillis = idleThresholdMillis;
    }

    @Override
    public boolean allow(String key) {
        TokenBucket bucket = getOrCreateBucket(key);
        synchronized (bucket) {
            long now = System.currentTimeMillis();
            bucket.refill(now, capacity, refillRate);
            return bucket.tryConsume();
        }

    }
    private TokenBucket getOrCreateBucket(String key) {
        TokenBucket bucket = tokenBuckets.computeIfAbsent(key, k -> new TokenBucket(this.capacity));
        return bucket;
    }
    private void cleanupIdleBuckets() {
        long now = System.currentTimeMillis();
        this.tokenBuckets.forEach(
            (key, bucket) -> {
                synchronized (bucket) {
                    long lastSeen = bucket.lastRefillTime;
                    if (now - lastSeen > idleThresholdMillis) {
                        tokenBuckets.remove(key);
                    }
                }
            }
        );
    }
    @Override
    public void start() {
        scheduler.scheduleAtFixedRate(this::cleanupIdleBuckets, cleanupIntervalMillis, cleanupIntervalMillis, TimeUnit.MILLISECONDS);
    }

    @Override 
    public void stop() {
        scheduler.shutdownNow();
    }
}