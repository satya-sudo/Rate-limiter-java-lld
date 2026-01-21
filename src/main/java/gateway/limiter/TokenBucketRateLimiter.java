package gateway.limiter;

import java.util.concurrent.ConcurrentHashMap;

public class TokenBucketRateLimiter  implements RateLimiter{
    private final double capacity;
    private final double refillRate;
    private final ConcurrentHashMap<String , TokenBucket> tokenBuckets =  new ConcurrentHashMap<>();

    public TokenBucketRateLimiter(double capacity, double refillRate){
        this.capacity = capacity;
        this.refillRate = refillRate;
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
}