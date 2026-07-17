package com.infy.newgen.config;

import java.util.concurrent.TimeUnit;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        // NO CACHING FOR OTP & EMAIL (Short Life, Transactional & High Risk
        // Information)

        // 1. Agent Cache - Long-Lived (12 Hours) -> Doesn't Change Frequently
        cacheManager.registerCustomCache("agentCache",
                Caffeine.newBuilder()
                        .maximumSize(1000)
                        .expireAfterWrite(12, TimeUnit.HOURS)
                        .build());

        // 2. Customer Cache - Medium-Lived (30 Mins From Last Access Time) -> Moderate
        // Change Frequency
        cacheManager.registerCustomCache("customerCache",
                Caffeine.newBuilder()
                        .maximumSize(5000)
                        .expireAfterWrite(30, TimeUnit.MINUTES)
                        .build());

        // 3. Policy Cache - Short-Lived (10 Mins) -> Changes Frequently
        cacheManager.registerCustomCache("policyCache",
                Caffeine.newBuilder()
                        .maximumSize(2000)
                        .expireAfterWrite(10, TimeUnit.MINUTES)
                        .build());
        return cacheManager;
    }
}
