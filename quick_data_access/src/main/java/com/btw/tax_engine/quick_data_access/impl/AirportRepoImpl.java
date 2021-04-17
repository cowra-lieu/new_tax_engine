package com.btw.tax_engine.quick_data_access.impl;

import com.btw.tax_engine.quick_data_access.AirportRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AirportRepoImpl implements AirportRepo {

    private RedisTemplate<String, String> rt;

    @Autowired
    @Qualifier("redisTemplate")
    public void setRedisTemplate(RedisTemplate<String, String> redisTemplate) {
        this.rt = redisTemplate;
    }

    @Override
    @Cacheable(value = "apcCache")
    public String getRawValue(String key) {
        String result = (String)rt.opsForHash().get("APC", key);
        return result == null ? "" : result;
    }

    @Override
    @Cacheable(value = "taxnameCache")
    public String getTaxname(String key) {
        String result = (String)rt.opsForHash().get("taxname2", key);
        return result == null ? (key.endsWith("zh")? "机场服务费":"Airport Service Fee") : result;
    }

    @Override
    @Cacheable(value = "raCache")
    public String getRAInfo(String key) {
        String result = (String)rt.opsForHash().get("RA", key);
        return result == null ? "" : result;
    }

}
