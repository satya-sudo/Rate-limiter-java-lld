# Rate Limiter Gateway (Java)

A simple **API gateway–style rate limiter** implemented in Java using the **Token Bucket algorithm**.

The service sits in front of an API endpoint and controls how many requests a user can make over time.

---

## What is this?

This project is an **in-memory, per-user rate limiter** built on top of Java’s built-in HTTP server.

- Each user is identified by the `X-User-Id` request header
- Each user has an independent token bucket
- Requests consume tokens from the bucket
- If no token is available, the request is rejected with `429 Too Many Requests`

---

## How does it work?

- Each user has a **token bucket**
- The bucket starts full (`capacity`)
- Tokens refill over time at a fixed rate (`refillRate`)
- Each request consumes **1 token**
- When tokens run out, requests are blocked

Token refill happens **lazily** on incoming requests.

---

## Current Rate Limit

```

Capacity:   5 tokens
RefillRate: 0.5 tokens per second

````

This allows:
- 5 immediate requests
- 1 additional request every 2 seconds afterward

---

## How is it implemented?

- **Token Bucket algorithm**
- **ConcurrentHashMap** for per-user buckets
- **Per-bucket synchronization** for thread safety
- **ScheduledExecutorService** for background cleanup
- **HTTP middleware** implemented by wrapping `HttpHandler`
- **Graceful shutdown** using JVM shutdown hooks

No frameworks (Spring, etc.) are used.

---

## How to run

Compile and run:

```bash
javac src/main/java/gateway/Main.java
java gateway.Main
````

The server listens on:

```
http://localhost:8080
```

---

## Test

```bash
curl -H "X-User-Id: test-user" localhost:8080/api
```

If the rate limit is exceeded:

```
HTTP 429 Too Many Requests
```

---

## Why this exists

This project demonstrates:

* low-level system design
* concurrency handling in Java
* time-based rate limiting algorithms
* clean separation between core logic and HTTP layer
* lifecycle management of background tasks

