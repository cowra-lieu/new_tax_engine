package com.btw.tax_engine.quick_data_access.impl;

import com.btw.tax_engine.quick_data_access.TAirportCustRepo;
import com.btw.tax_engine.quick_data_access.config.LettuceRedisConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class TAirportCustRepoImpl implements TAirportCustRepo {

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
    @Cacheable(value = "tpt1Cache")
    public boolean isMemberOfPT1(String key) {
        RedisTemplate<String, String> r = LettuceRedisConfig.tUseA ? rt : rt_b;
        Boolean result = r.opsForSet().isMember("PT1", key);
        return result != null && result;
    }

    @Override
    @Cacheable(value = "tpt2Cache")
    public boolean isMemberOfPT2(String key) {
        RedisTemplate<String, String> r = LettuceRedisConfig.tUseA ? rt : rt_b;
        Boolean result = r.opsForSet().isMember("PT2", key);
        return result != null && result;
    }

    @Override
    @Cacheable(value = "tpt3Cache")
    public boolean isMemberOfPT3(String key) {
        RedisTemplate<String, String> r = LettuceRedisConfig.tUseA ? rt : rt_b;
        Boolean result = r.opsForSet().isMember("PT3", key);
        return result != null && result;
    }

    @Override
    @Cacheable(value = "tpj1Cache")
    public boolean isMemberOfPJ1(String key) {
        RedisTemplate<String, String> r = LettuceRedisConfig.tUseA ? rt : rt_b;
        Boolean result = r.opsForSet().isMember("PJ1", key);
        return result != null && result;
    }

    @Override
    @Cacheable(value = "tpj2Cache")
    public boolean isMemberOfPJ2(String key) {
        RedisTemplate<String, String> r = LettuceRedisConfig.tUseA ? rt : rt_b;
        Boolean result = r.opsForSet().isMember("PJ2", key);
        return result != null && result;
    }

    @Override
    @Cacheable(value = "tpjvCache")
    public boolean isMemberOfPJV(String key) {
        RedisTemplate<String, String> r = LettuceRedisConfig.tUseA ? rt : rt_b;
        Boolean result = r.opsForSet().isMember("PJV", key);
        return result != null && result;
    }

    @Override
    @Cacheable(value = "tpjwCache")
    public boolean isMemberOfPJW(String key) {
        RedisTemplate<String, String> r = LettuceRedisConfig.tUseA ? rt : rt_b;
        Boolean result = r.opsForSet().isMember("PJW", key);
        return result != null && result;
    }

}
