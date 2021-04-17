package com.btw.tax_engine.quick_data_access.impl;

import com.btw.tax_engine.quick_data_access.CurrRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CurrRepoImpl implements CurrRepo {

    private RedisTemplate<String, String> rt;

    @Autowired
    @Qualifier("redisTemplate")
    public void setRedisTemplate(RedisTemplate<String, String> redisTemplate) {
        this.rt = redisTemplate;
    }

    @Override
    @Cacheable(value = "currCache")
    public String getRawValue(String key) {
        return (String)rt.opsForHash().get("CURR", key);
    }
}
