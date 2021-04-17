package com.btw.tax_engine.quick_data_access.impl;

import com.btw.tax_engine.quick_data_access.CabinRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import static com.btw.tax_engine.common.Const.NC;

@Repository
public class CabinRepoImpl implements CabinRepo {

    private RedisTemplate<String, String> rt;

    @Autowired
    @Qualifier("redisTemplate")
    public void setRedisTemplate(RedisTemplate<String, String> redisTemplate) {
        this.rt = redisTemplate;
    }

    @Override
    @Cacheable(value = "cabCache", key = "#p0 + #p1 + #p2")
    public char getCabin(String carrierCode, char primeCode, int bookingDate) {
        char cabin = NC;
        String key = primeCode+carrierCode;
        String datesAndCabin = (String)rt.opsForHash().get("CABIN", key);
        if (datesAndCabin != null) {
            if (Integer.parseInt(datesAndCabin.substring(0, 8)) <= bookingDate
                    && Integer.parseInt(datesAndCabin.substring(8, 16)) >= bookingDate) {
                cabin = datesAndCabin.charAt(16);
            }
        }
        return cabin;
    }

}
