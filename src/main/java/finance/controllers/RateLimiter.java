package finance.controllers;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bandwidth;
import java.time.Duration;

public class RateLimiter {

    private final Bucket bucket;

    public RateLimiter(int requests, Duration duration) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(requests)
                .refillIntervally(requests, duration)
                .build();

        this.bucket = Bucket.builder()
                .addLimit(limit)
                .build();
    }

    public boolean allowRequest() {
        return bucket.tryConsume(1);
    }
}
