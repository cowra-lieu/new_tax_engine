package com.btw.tax_engine.quick_data_access.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

@Service
public class CacheServiceImpl implements CacheService {

    private static final Logger log = LoggerFactory.getLogger(CacheServiceImpl.class);

    @Override
    @CacheEvict(value = "itineraryCache", allEntries = true)
    public void evictAllItineraryCache() {
        log.warn("all itinerary caches have been evicted.");
    }

    @Override
    @CacheEvict(value = "itinerarySectorCache", allEntries = true)
    public void evictAllItinerarySectorCache() {
        log.warn("all itinerary_sector caches have been evicted.");
    }

    @Override
    @CacheEvict(value = "PJLCache", allEntries = true)
    public void evictAllPJLCache() {
        log.warn("all PJLCache have been evicted.");
    }

    @Override
    @CacheEvict(value = "PSLCache", allEntries = true)
    public void evictAllPSLCache() {
        log.warn("all PSLCache have been evicted.");
    }

    @Override
    @CacheEvict(value = "PSVCache", allEntries = true)
    public void evictAllPSVCache() {
        log.warn("all PSVCache have been evicted.");
    }

    @Override
    @CacheEvict(value = "y198Cache", allEntries = true)
    public void evictAllY198Cache() {
        log.warn("all y198Cache have been evicted.");
    }

    @Override
    @CacheEvict(value = "checkLocationCache", allEntries = true)
    public void evictAllcheckLocationCache() {
        log.warn("all checkLocationCache have been evicted.");
    }

    @Override
    @CacheEvict(value = "tpt1Cache", allEntries = true)
    public void evictAllPT1Cache() {
        log.warn("all tpt1Cache have been evicted.");
    }
    @Override
    @CacheEvict(value = "tpt2Cache", allEntries = true)
    public void evictAllPT2Cache() {
        log.warn("all tpt2Cache have been evicted.");
    }
    @Override
    @CacheEvict(value = "tpt3Cache", allEntries = true)
    public void evictAllPT3Cache() {
        log.warn("all tpt3Cache have been evicted.");
    }
    @Override
    @CacheEvict(value = "tpj1Cache", allEntries = true)
    public void evictAllPJ1Cache() {
        log.warn("all tpj1Cache have been evicted.");
    }
    @Override
    @CacheEvict(value = "tpj2Cache", allEntries = true)
    public void evictAllPJ2Cache() {
        log.warn("all tpj2Cache have been evicted.");
    }
    @Override
    @CacheEvict(value = "tpjvCache", allEntries = true)
    public void evictAllPJVCache() {
        log.warn("all tpjvCache have been evicted.");
    }
    @Override
    @CacheEvict(value = "tpjwCache", allEntries = true)
    public void evictAllPJWCache() {
        log.warn("all tpjwCache have been evicted.");
    }


    @Override
    @CacheEvict(value = "t167XCache", allEntries = true)
    public void evictAllT167XCache() {
        log.warn("all t167XCache have been evicted.");
    }

    @Override
    @CacheEvict(value = "t167YCache", allEntries = true)
    public void evictAllT167YCache() {
        log.warn("all t167YCache have been evicted.");
    }

    @Override
    @CacheEvict(value = "t168Cache", allEntries = true)
    public void evictAllT168Cache() {
        log.warn("all t168Cache have been evicted.");
    }

    @Override
    @CacheEvict(value = "t169Cache", allEntries = true)
    public void evictAllT169Cache() {
        log.warn("all t169Cache have been evicted.");
    }
    @Override
    @CacheEvict(value = "c2ssCache", allEntries = true)
    public void evictAllC2SSCache() {
        log.warn("all c2ssCache have been evicted.");
    }
    @Override
    @CacheEvict(value = "c2nCache", allEntries = true)
    public void evictAllC2NCache() {
        log.warn("all c2nCache have been evicted.");
    }
    @Override
    @CacheEvict(value = "n2aCache", allEntries = true)
    public void evictAllN2ACache() {
        log.warn("all n2aCache have been evicted.");
    }
    @Override
    @CacheEvict(value = "ss2aCache", allEntries = true)
    public void evictAllSS2ACache() {
        log.warn("all ss2aCache have been evicted.");
    }
    @Override
    @CacheEvict(value = "s2aCache", allEntries = true)
    public void evictAllS2ACache() {
        log.warn("all s2aCache have been evicted.");
    }
    @Override
    @CacheEvict(value = "z2aCache", allEntries = true)
    public void evictAllZ2ACache() {
        log.warn("all z2aCache have been evicted.");
    }

    @Override
    @CacheEvict(value = "raCache", allEntries = true)
    public void evictAllraCache() {
        log.warn("all raCache have been evicted.");
    }

    @Override
    @CacheEvict(value = "t183Cache", allEntries = true)
    public void evictAllT183Cache() {
        log.warn("all t183Cache have been evicted.");
    }

    @Override
    @CacheEvict(value = "t186Cache", allEntries = true)
    public void evictAllT186Cache() {
        log.warn("all t186Cache have been evicted.");
    }

    @Override
    @CacheEvict(value = "t190Cache", allEntries = true)
    public void evictAllT190Cache() {
        log.warn("all t190Cache have been evicted.");
    }

    @Override
    @CacheEvict(value = "icerCache", allEntries = true)
    public void evictAllicerCache() {
        log.warn("all icerCache have been evicted.");
    }

    @Override
    @CacheEvict(value = "icerFlagCache", allEntries = true)
    public void evictAllicerFlagCache() {
        log.warn("all icerFlagCache have been evicted.");
    }

}
