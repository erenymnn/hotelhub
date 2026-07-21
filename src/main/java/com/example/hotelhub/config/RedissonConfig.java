package com.example.hotelhub.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    // Properties veya Docker üzerinden gelen host bilgisini alıyoruz
    @Value("${spring.data.redis.host}")
    private String redisHost;

    // Properties veya Docker üzerinden gelen port bilgisini alıyoruz
    @Value("${spring.data.redis.port}")
    private String redisPort;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();

        // Dinamik olarak adresi oluşturuyoruz (Örn: redis://redis-cache:6379 veya redis://localhost:6379)
        String redisAddress = "redis://" + redisHost + ":" + redisPort;

        config.useSingleServer().setAddress(redisAddress);
        return Redisson.create(config);
    }
}