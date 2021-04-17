package com.btw.tax_engine.quick_data_access.impl;

import com.btw.tax_engine.common.SU;
import com.btw.tax_engine.quick_data_access.IcerRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

import static com.btw.tax_engine.common.Const.ICER_KEY_LIST;

@Repository
public class IcerRepoImpl implements IcerRepo {

    private static final Logger log = LoggerFactory.getLogger(IcerRepoImpl.class);

    private RedisTemplate<String, String> rt;

    @Autowired
    @Qualifier("redisTemplate")
    public void setRedisTemplate(RedisTemplate<String, String> redisTemplate) {
        this.rt = redisTemplate;
    }

    @Override
    @Cacheable(value = "icerCache", key = "#p0 + #p1 + #p2")
    public double getRate(String key, String from, String to) {
        log.debug("hit redis cache");
        String value = (String)rt.opsForHash().get(key,from + to);
        if (SU.blk(value)) {
            return 1;
        } else {
            return Double.parseDouble(value);
        }
    }

    @Override
    @Cacheable(value = "icerFlagCache")
    public int[] getIcerFlags() {
        log.debug("hit redis cache");
        int len = ICER_KEY_LIST.size();
        int[] result = new int[len];
        List<Object> dateList = rt.opsForHash().multiGet("ICER_FLAG", ICER_KEY_LIST);
        for (int i=0; i<len; i++) {
            result[i] = Integer.parseInt(((String)dateList.get(i)).replaceAll("-", ""));
        }
        return result;
    }
}
