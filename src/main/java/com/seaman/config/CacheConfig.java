package com.seaman.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.seaman.constant.BusinessConstant;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.Duration;
import java.util.Arrays;

@Configuration
public class CacheConfig {

    @Bean
    public SimpleCacheManager buildCacheManager(){
        CaffeineCache masterMessageCode = buildCaffeineCache(BusinessConstant.MASTER_MESSAGE_CODE, BusinessConstant.CACHE_GENERAL_EXPIRE_HOURS);
        CaffeineCache masterProvinces = buildCaffeineCache(BusinessConstant.MASTER_PROVINCES, BusinessConstant.CACHE_GENERAL_EXPIRE_HOURS);
        CaffeineCache masterDocument = buildCaffeineCache(BusinessConstant.MASTER_DOCUMENT, BusinessConstant.CACHE_GENERAL_EXPIRE_HOURS);
        CaffeineCache masterDocumentRenewalStatus = buildCaffeineCache(BusinessConstant.MASTER_DOCUMENT_RENEWAL_STATUS, BusinessConstant.CACHE_GENERAL_EXPIRE_HOURS);
        SimpleCacheManager simpleCacheManager = new SimpleCacheManager();
        simpleCacheManager.setCaches(Arrays.asList(masterMessageCode, masterProvinces,
                masterDocument, masterDocumentRenewalStatus));
        simpleCacheManager.initializeCaches();

        return simpleCacheManager;
    }

    private CaffeineCache buildCaffeineCache(String name, int expireHours){
        return new CaffeineCache(name, Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofHours(expireHours))
                .build());
    }

}
