package com.btw.tax_engine.computing_core;

public interface AirportInfoService {

    String getCity(String apc);
    String getNation(String apc);
    String getZone(String apc);
    String getArea(String apc);

    String getValue(String apc);

}
