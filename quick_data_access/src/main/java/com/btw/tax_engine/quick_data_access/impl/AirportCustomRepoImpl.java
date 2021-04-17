package com.btw.tax_engine.quick_data_access.impl;

import com.btw.tax_engine.common.DEU;
import static com.btw.tax_engine.common.Const.*;
import com.btw.tax_engine.quick_data_access.AirportCustomRepo;
import com.btw.tax_engine.quick_data_access.config.LettuceRedisConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AirportCustomRepoImpl implements AirportCustomRepo {

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
    @Cacheable(value = "PJLCache", key = "#p0+#p1+#p2+#p3")
    public boolean isEffectivePJL(String marketingCarrierCode, String airport, String customNo,
                                  int bookingDate) {
        RedisTemplate<String, String> r = LettuceRedisConfig.yUseA ? rt : rt_b;
        String v = (String)r.opsForHash().get("PJL",
                marketingCarrierCode + COLON + airport + COLON + customNo);
        return (v != null) &&  DEU.between(v.substring(0, 8), v.substring(8), bookingDate);
    }

    @Override
    @Cacheable(value = "PSLCache", key = "#p0+#p1+#p2+#p3")
    public boolean isEffectivePSL(String marketingCarrierCode, String airport, String customNo,
                                  int bookingDate) {
        RedisTemplate<String, String> r = LettuceRedisConfig.yUseA ? rt : rt_b;
        String v = (String)r.opsForHash().get("PSL",
                marketingCarrierCode + COLON + airport + COLON +customNo);
        return (v != null) &&  DEU.between(v.substring(0, 8), v.substring(8), bookingDate);
    }

    @Override
    @Cacheable(value = "PSVCache", key = "#p0+#p1+#p2+#p3")
    public boolean isEffectivePSV(String marketingCarrierCode, String airport, String customNo,
                                  int bookingDate) {
        RedisTemplate<String, String> r = LettuceRedisConfig.yUseA ? rt : rt_b;
        String v = (String)r.opsForHash().get("PSV",
                marketingCarrierCode + COLON + airport + COLON + customNo);
        return (v != null) &&  DEU.between(v.substring(0, 8), v.substring(8), bookingDate);
    }

}
