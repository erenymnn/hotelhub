package com.example.hotelhub.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

@Configuration
public class RedisConfig {

    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                // Veriler RAM'de sonsuza kadar kalmasın, 60 dakika sonra otomatik silinsin
                .entryTtl(Duration.ofMinutes(60))

                // Eğer veritabanından Null (boş) bir şey dönerse, Redis'i bununla meşgul etme
               //olmasa hacker birsürü istek atıp rami şişirir.
                .disableCachingNullValues()

                // Verileri anlamsız byte'lar yerine temiz JSON olarak kaydet!
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
    }
}