packege gateway.middleware;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.OutputStream;
import java.io.IOException;

import gateway.limiter.RateLimiter;

public class RateLimiterMiddleware extends HttpHandler {
    private final RateLimiter limiter;
    private final HttpHandler next;


    public RateLimiterMiddleware(RateLimiter limiter, HttpHandler next) {
        this.limiter = limiter;
        this.next = next;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException  {
        String userId = exchange.getRequestHeaders().getFirst("X-User-Id");

        if (userId == null || userId.isEmpty()) {
            userId = exchange.getRemoteAddress().getAddress().getHostAddress();
        }
        if (!limiter.allow(userId)) {
            String response = "too many requests";
            exchange.sendResponseHeaders(429, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
            return;
        }
        next.handle(exchange);
    }
}