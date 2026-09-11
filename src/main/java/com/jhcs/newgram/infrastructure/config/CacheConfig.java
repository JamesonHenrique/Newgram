package com.jhcs.newgram.infrastructure.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.TimeUnit;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cache local Caffeine (single-instance). Contagens e listas quentes com
 * TTL curto; invalidação explícita nas escritas (seguir, post novo).
 * Multi-instância: trocar por Redis sem mudar callers.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String CONTAGENS_SEGUIDORES = "contagens-seguidores";
    public static final String HASHTAGS_POPULARES = "hashtags-populares";

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager(CONTAGENS_SEGUIDORES, HASHTAGS_POPULARES);
        manager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .recordStats());
        return manager;
    }
}
