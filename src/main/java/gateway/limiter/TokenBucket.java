package gateway.limiter;

public class TokenBucket {
    double tokens;
    long lastRefillTime;

    public TokenBucket(double capacity) {
        this.tokens = capacity;
        this.lastRefillTime = System.currentTimeMillis();
    }

    public boolean tryConsume(){
        if (this.tokens >= 1.0) {
            this.tokens--;
            return true;
        }
        return false;
    }
    public void refill(long now, double capacity, double refillRate) {
        double timeLapse = ((now-this.lastRefillTime) / 1000.0);
        double newTokenCount = this.tokens + (timeLapse * refillRate);
        this.tokens = Math.min(capacity, newTokenCount);
        this.lastRefillTime = now;
    }
}