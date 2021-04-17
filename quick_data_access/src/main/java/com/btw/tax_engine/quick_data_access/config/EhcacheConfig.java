package com.btw.tax_engine.quick_data_access.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.cache.jcache.JCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import org.springframework.cache.CacheManager;
import java.io.IOException;

@Configuration
@EnableCaching
public class EhcacheConfig extends CachingConfigurerSupport {

    private static final Logger log = LoggerFactory.getLogger(EhcacheConfig.class);

    @Bean
    CacheManager jCacheCacheManager(javax.cache.CacheManager cacheManager) {
        return new JCacheCacheManager(cacheManager);
    }

    @Bean
    JCacheManagerFactoryBean cacheManagerFactoryBean() {
        JCacheManagerFactoryBean jcmfb = new JCacheManagerFactoryBean();
        try {
            jcmfb.setCacheManagerUri(new ClassPathResource("/config/ehcache.xml").getURI());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        return jcmfb;
    }

}
