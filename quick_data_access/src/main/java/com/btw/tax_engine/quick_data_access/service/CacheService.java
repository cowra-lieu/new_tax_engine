package com.btw.tax_engine.quick_data_access.service;

public interface CacheService {

    void evictAllItineraryCache();
    void evictAllItinerarySectorCache();

    void evictAllcheckLocationCache();

    void evictAllPJLCache();
    void evictAllPSLCache();
    void evictAllPSVCache();
    void evictAllY198Cache();

    void evictAllPT1Cache();
    void evictAllPT2Cache();
    void evictAllPT3Cache();
    void evictAllPJ1Cache();
    void evictAllPJ2Cache();
    void evictAllPJVCache();
    void evictAllPJWCache();

    void evictAllT167XCache();
    void evictAllT167YCache();
    void evictAllT168Cache();
    void evictAllT169Cache();
    void evictAllT183Cache();
    void evictAllT186Cache();
    void evictAllT190Cache();

    void evictAllC2SSCache();
    void evictAllC2NCache();
    void evictAllN2ACache();
    void evictAllSS2ACache();
    void evictAllS2ACache();
    void evictAllZ2ACache();

    void evictAllraCache();

    void evictAllicerCache();
    void evictAllicerFlagCache();

}
