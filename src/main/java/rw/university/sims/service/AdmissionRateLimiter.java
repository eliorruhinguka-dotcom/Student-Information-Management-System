package rw.university.sims.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

/** Per-instance guard for the public application endpoint. For multi-instance production deployments,
 * replace this with a shared Redis/API-gateway limiter. */
@Component
public class AdmissionRateLimiter {
    private static final int MAX_REQUESTS = 5;
    private static final long WINDOW_SECONDS = 60;
    private final ConcurrentHashMap<String, Deque<Instant>> attempts = new ConcurrentHashMap<>();

    public void check(String ipAddress) {
        String key = (ipAddress == null || ipAddress.isBlank()) ? "unknown" : ipAddress;
        Instant now = Instant.now();
        Deque<Instant> queue = attempts.computeIfAbsent(key, ignored -> new ArrayDeque<>());
        synchronized (queue) {
            while (!queue.isEmpty() && queue.peekFirst().isBefore(now.minusSeconds(WINDOW_SECONDS))) {
                queue.removeFirst();
            }
            if (queue.size() >= MAX_REQUESTS) {
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                        "Too many applications from this network. Try again later.");
            }
            queue.addLast(now);
        }
    }
}
