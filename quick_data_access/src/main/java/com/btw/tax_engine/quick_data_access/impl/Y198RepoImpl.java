package com.btw.tax_engine.quick_data_access.impl;

import static com.btw.tax_engine.common.SU.*;
import com.btw.tax_engine.quick_data_access.Y198Repo;
import com.btw.tax_engine.quick_data_access.config.LettuceRedisConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class Y198RepoImpl implements Y198Repo {

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
    @Cacheable(value = "y198Cache", key = "#p0 + #p1 + #p2 + #p3")
    public boolean checkRBDsFrom198(String tableNo, String carrier, char clazz, int bookingDate) {
        boolean result = false;
        String key = tableNo+'M'+carrier;
        RedisTemplate<String, String> r = LettuceRedisConfig.yUseA ? rt : rt_b;
        String datesAndRDBs = (String)r.opsForHash().get("Y198", key);
        if (!blk(datesAndRDBs)) {
            int n = 0;
            String sec;
            while (n < Integer.MAX_VALUE) {
                sec = nthsec(datesAndRDBs, ',', n++);
                if (blk(sec)) {
                    break;
                }
                if (Integer.parseInt(sec.substring(0, 8)) <= bookingDate
                        && Integer.parseInt(sec.substring(8, 16)) >= bookingDate
                        && sec.indexOf(clazz, 16) > 0) {
                    result = true;
                    break;
                }
            }

        }
        return result;
    }

}
