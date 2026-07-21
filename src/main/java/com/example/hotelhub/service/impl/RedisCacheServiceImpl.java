package com.example.hotelhub.service.impl;

import com.example.hotelhub.service.RedisCacheService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.Random;
import java.util.concurrent.TimeUnit;
@Service
public class RedisCacheServiceImpl implements RedisCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final Random random = new Random();

    // Micrometer sayaçları
    private final Counter cacheHitCounter;
    private final Counter cacheMissCounter;

    public RedisCacheServiceImpl(RedisTemplate<String, Object> redisTemplate, MeterRegistry registry) {
        this.redisTemplate = redisTemplate;

        // DÜZELTİLDİ: (Counter) cast işlemi kaldırıldı, doğrudan Micrometer sayacı alınıyor
        this.cacheHitCounter = registry.counter("hotelhub_cache_requests_total", "result", "hit");
        this.cacheMissCounter = registry.counter("hotelhub_cache_requests_total", "result", "miss");
    }

    @Override
    public void saveWithJitter(String key, Object value) {
        int baseTtl = 60;
        int jitter = random.nextInt(15) + 1;
        redisTemplate.opsForValue().set(key, value, baseTtl + jitter, TimeUnit.MINUTES);
    }

    @Override
    public Object getFromCache(String key) {
        Object value = redisTemplate.opsForValue().get(key);

        if (value != null) {

            cacheHitCounter.increment();
        } else {
            cacheMissCounter.increment();
        }

        return value;
    }
}