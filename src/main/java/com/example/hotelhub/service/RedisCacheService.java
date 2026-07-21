package com.example.hotelhub.service;

public interface RedisCacheService {
    void saveWithJitter(String key, Object value);

    Object getFromCache(String key);
}
