package finance.controllers;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bandwidth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RateLimiter {
    private final ConcurrentMap<String, Bucket> buckets =
            new ConcurrentHashMap<>();

    private final int requests;
    private final Duration duration;
    private final Environment environment;


    public RateLimiter(int requests, Duration duration, Environment environment) {
        this.requests = requests;
        this.duration = duration;
        this.environment = environment;
    }

    public Boolean allowRequest(String key){
        if (environment.acceptsProfiles(Profiles.of("test"))) {
            return true;
        }
        if (environment.acceptsProfiles(Profiles.of("test"))) return true;
        Bucket bucket = buckets.computeIfAbsent(key, k -> createBucket());
        return bucket.tryConsume(1);
    };

    private Bucket createBucket(){
        Bandwidth limit = Bandwidth.builder()
                .capacity(requests)
                .refillIntervally(requests, duration)
                .build();

        return Bucket.builder()
                .addLimit(limit)
                .build();
    };
}
