package com.example.hotelhub.service.impl;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisCacheServiceImplTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private MeterRegistry meterRegistry;

    @Mock
    private Counter cacheHitCounter;

    @Mock
    private Counter cacheMissCounter;

    private RedisCacheServiceImpl redisCacheService;

    @BeforeEach
    void setUp() {
        when(meterRegistry.counter("hotelhub_cache_requests_total", "result", "hit")).thenReturn(cacheHitCounter);
        when(meterRegistry.counter("hotelhub_cache_requests_total", "result", "miss")).thenReturn(cacheMissCounter);
        
        redisCacheService = new RedisCacheServiceImpl(redisTemplate, meterRegistry);
    }

    @Test
    void saveWithJitter_ShouldSaveWithRandomTimeout() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        redisCacheService.saveWithJitter("testKey", "testValue");

        verify(redisTemplate, times(1)).opsForValue();
        // Since timeout is random (61-75), we just verify it uses any long and TimeUnit.MINUTES
        verify(valueOperations, times(1)).set(eq("testKey"), eq("testValue"), anyLong(), eq(TimeUnit.MINUTES));
    }

    @Test
    void getFromCache_WhenKeyExists_ShouldReturnObjectAndIncrementHit() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("testKey")).thenReturn("cachedValue");

        Object result = redisCacheService.getFromCache("testKey");

        assertEquals("cachedValue", result);
        verify(cacheHitCounter, times(1)).increment();
        verify(cacheMissCounter, never()).increment();
    }

    @Test
    void getFromCache_WhenKeyNotExists_ShouldReturnNullAndIncrementMiss() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("testKey")).thenReturn(null);

        Object result = redisCacheService.getFromCache("testKey");

        assertNull(result);
        verify(cacheMissCounter, times(1)).increment();
        verify(cacheHitCounter, never()).increment();
    }
}
