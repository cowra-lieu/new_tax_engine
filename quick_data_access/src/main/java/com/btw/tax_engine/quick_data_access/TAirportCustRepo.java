package com.btw.tax_engine.quick_data_access;

public interface TAirportCustRepo {

    boolean isMemberOfPT1(String key);
    boolean isMemberOfPT2(String key);
    boolean isMemberOfPT3(String key);
    boolean isMemberOfPJ1(String key);
    boolean isMemberOfPJ2(String key);
    boolean isMemberOfPJV(String key);
    boolean isMemberOfPJW(String key);
}
