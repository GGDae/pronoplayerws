package com.pronoplayer.app.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizer;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.stereotype.Component;

@Component
public class SpringCacheCustomizer implements CacheManagerCustomizer<ConcurrentMapCacheManager> {

    @Override
    public void customize(ConcurrentMapCacheManager cacheManager) {
        List<String> caches = new ArrayList<>();
        caches.add("ranking");
        cacheManager.setCacheNames(caches);
    }
}