package com.btw.tax_engine.quick_data_access.impl;

import com.btw.tax_engine.quick_data_access.T169Repo;
import com.btw.tax_engine.quick_data_access.config.LettuceRedisConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class T169RepoImpl implements T169Repo {

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
    @Cacheable(value = "t169Cache")
    public String getRawValue(String key) {
        RedisTemplate<String, String> r = LettuceRedisConfig.tUseA ? rt : rt_b;
        String result = (String)r.opsForHash().get("T169", key);
        return result == null ? "" : result;
    }

    @Override
    @Cacheable(value = "c2ssCache")
    public String c2ss(String city) {
        RedisTemplate<String, String> r = LettuceRedisConfig.tUseA ? rt : rt_b;
        String result = (String)r.opsForHash().get("C2SS", city);
        return result == null ? "" : result;
    }

    @Override
    @Cacheable(value = "c2nCache")
    public String c2n(String city) {
        RedisTemplate<String, String> r = LettuceRedisConfig.tUseA ? rt : rt_b;
        String result = (String)r.opsForHash().get("C2N", city);
        return result == null ? "" : result;
    }

    @Override
    @Cacheable(value = "n2aCache")
    public String n2a(String nation) {
        RedisTemplate<String, String> r = LettuceRedisConfig.tUseA ? rt : rt_b;
        String result = (String)r.opsForHash().get("N2A", nation);
        return result == null ? "" : result;
    }

    @Override
    @Cacheable(value = "ss2aCache")
    public String ss2a(String substate) {
        RedisTemplate<String, String> r = LettuceRedisConfig.tUseA ? rt : rt_b;
        String result = (String)r.opsForHash().get("SS2A", substate);
        return result == null ? "" : result;
    }

    @Override
    @Cacheable(value = "s2aCache")
    public String s2a(String state) {
        RedisTemplate<String, String> r = LettuceRedisConfig.tUseA ? rt : rt_b;
        String result = (String)r.opsForHash().get("S2A", state);
        return result == null ? "" : result;
    }

    @Override
    @Cacheable(value = "z2aCache")
    public String z2a(String zone) {
        RedisTemplate<String, String> r = LettuceRedisConfig.tUseA ? rt : rt_b;
        String result = (String)r.opsForHash().get("Z2A", zone);
        return result == null ? "" : result;
    }
}
