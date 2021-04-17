package com.btw.tax_engine.quick_data_access;

public interface AirportRepo {

    String getRawValue(String key);

    String getTaxname(String key);

    String getRAInfo(String key);
}
