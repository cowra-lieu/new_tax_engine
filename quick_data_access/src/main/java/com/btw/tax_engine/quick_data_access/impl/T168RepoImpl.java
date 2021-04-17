package com.btw.tax_engine.quick_data_access.impl;

import com.btw.tax_engine.quick_data_access.T168Repo;
import com.btw.tax_engine.quick_data_access.config.LettuceRedisConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class T168RepoImpl implements T168Repo {

    private RedisTemplate<String, String> rt;
    private RedisTemplate<String, String> rt_b;

    @Autowired
    @Qualifier("redisTemplate")
    public void setRedisTemplate(RedisTemplate<String, String> redisTemplate) {
        this.rt = redisTemplate;
    }

    @Autowired
    @Qualifier("redisTemplate_b")
    public void setRedisTemplate_b(RedisTemplate<String, String> redisTemplate) {
        this.rt_b = redisTemplate;
    }

    @Override
    @Cacheable(value = "t168Cache")
    public String getRawValue(String key) {
        RedisTemplate<String, String> r = LettuceRedisConfig.tUseA ? rt : rt_b;
        String result = (String)r.opsForHash().get("T168", key);
        return result == null ? "" : result;
    }

}
