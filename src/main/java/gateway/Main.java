package gateway;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import gateway.limiter.RateLimiter;
import gateway.limiter.ManagedLimiter;
import gateway.limiter.TokenBucketRateLimiter;
import gateway.middleware.RateLimiterMiddleware;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class Main{
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(
            new InetSocketAddress("0.0.0.0", 8080), 0
        );
        HttpHandler healthHandler = (httpExchange) -> {
            String response = "{\"Status\":\"Up\"}";
            httpExchange.getResponseHeaders().add("Content-Type", "application/json");
            httpExchange.sendResponseHeaders(200, response.getBytes().length);
            try (OutputStream os = httpExchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        };
        HttpHandler mockEndPoint = (httpExchange) -> {
            String response = "{\"Message\":\"you have reached the end point\"}";
            httpExchange.getResponseHeaders().add("Content-Type", "application/json");
            httpExchange.sendResponseHeaders(200, response.getBytes().length);
            try (OutputStream os = httpExchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        };
        server.createContext("/health", healthHandler);
        RateLimiter limiter = new TokenBucketRateLimiter( 5, 0.5,60_000,30_000);
        if (limiter instanceof ManagedLimiter m) {
            m.start();
            Runtime.getRuntime().addShutdownHook(new Thread(m::stop));
        }
        HttpHandler rateLimitedApi = new RateLimiterMiddleware(limiter, mockEndPoint);

        server.createContext("/api", rateLimitedApi);
        server.setExecutor(null);
        server.start();
    }
}