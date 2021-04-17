package com.btw.tax_engine.quick_data_access.impl;

import com.btw.tax_engine.quick_data_access.AirportRepo;
import com.btw.tax_engine.quick_data_access.TMPMRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

import static com.btw.tax_engine.common.Const.*;
import static com.btw.tax_engine.common.SU.*;

@Repository
public class TMPMRepoImpl implements TMPMRepo {

    private static final Logger log = LoggerFactory.getLogger(TMPMRepoImpl.class);

    private static final Map<String, String> GIMap = new HashMap<>();
    static {
        GIMap.put("11", "WH");
        GIMap.put("22", "EH");
        GIMap.put("33", "EH");
    }

    private AirportRepo airportDao;

    @Autowired
    public void setAirportDao(AirportRepo airportDao) {
        this.airportDao = airportDao;
    }

    private RedisTemplate<String, String> rt;

    @Autowired
    @Qualifier("redisTemplate")
    public void setRedisTemplate(RedisTemplate<String, String> redisTemplate) {
        this.rt = redisTemplate;
    }

    /**
     * Get TPM from Redis or DB
     * @param fromCode departure airport code
     * @param toCode arrival airport code
     * @return null or the most suitable value
     */
    @Override
    @Cacheable(value = "tpmCache", key = "#p0 + #p1")
    public double getTPM(String fromCode, String toCode) {
        return getPM(fromCode, toCode, "TPM");
    }

    @Override
    @Cacheable(value = "mpmCache", key="#p0 + #p1")
    public double getMPM(String fromCode, String toCode) {
        double result = getPM(fromCode, toCode, "MPM");
        return result / 1.2;
    }

    /**
     * Get TPM or MPM by specified parameters.
     *
     * @param fromCode the code of origin airport
     * @param toCode the code of another airport in itinerary
     * @param key main key of 'TPM' or 'MPM' in redis
     *
     * @return a double float number
     */
    private double getPM(String fromCode, String toCode, String key) {
        double result = 0;
        String fromAirportInfo = airportDao.getRawValue(fromCode);
        String toAirportInfo = airportDao.getRawValue(toCode);
        String c1 = rnthsec(fromAirportInfo, 2);
        String c2 = rnthsec(toAirportInfo, 2);
        String value = (String)rt.opsForHash().get(key, c1 + c2);
        String gi = null;
        if (null != value) {
            gi = getGI(fromAirportInfo, toAirportInfo);
        } else {
            value = (String)rt.opsForHash().get(key, c2 + c1);
            if (null != value) {
                gi = getGI(toAirportInfo, fromAirportInfo);
            }
        }
        if (null != value) {
            int i, j;
            if ("XX".equals(gi) || (i = value.indexOf(gi)) < 0) {
                result = Double.parseDouble(value.substring(3, (j=value.indexOf(','))<0?value.length():j ));
            } else {
                result = Double.parseDouble(
                        value.substring(i+3, (j=value.indexOf(',', i))<0?value.length():j)
                );
            }
        }
        return result;
    }

    @Override
//    @Cacheable(value = "gcmCache")
    public double getGCM(String fromCode, String toCode) {
        //TODO Here we need a real GCM
        log.warn("enter in GCM for {}-{}", fromCode, toCode);
        return 99999.0;
    }

    /**
     * Get global indicator by fromCode and toCode
     * @return the global indicator
     */
    private String getGI(String fromInfo, String toInfo) {
        //TODO the logic here need to be enhanced in future
        String result = "XX";
        String fromArea = nthsec(fromInfo, AS_AREA);
        String toArea = nthsec(toInfo, AS_AREA);
        String key = fromArea + toArea;
        if (GIMap.containsKey(key)) {
            result = GIMap.get(key);
        } else {
            log.debug("Unrecognizable Global Indicator for {} <-> {}",
                    fromInfo, toInfo);
        }
        return result;
    }
}
