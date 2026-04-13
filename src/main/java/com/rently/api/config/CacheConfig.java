package com.rently.api.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

  public static final String VEHICLES = "vehicles";
  public static final String VEHICLES_AVAILABLE = "vehicles-available";
  public static final String CUSTOMERS = "customers";

  @Bean
  CacheManager cacheManager() {
    CaffeineCacheManager manager = new CaffeineCacheManager(VEHICLES, VEHICLES_AVAILABLE, CUSTOMERS);
    manager.setCaffeine(Caffeine.newBuilder()
        .expireAfterWrite(60, TimeUnit.SECONDS)
        .maximumSize(500));
    return manager;
  }
}
