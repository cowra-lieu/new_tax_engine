package com.btw.tax_engine.quick_data_access;

public interface AirportCustomRepo {

    boolean isEffectivePJL(String marketingCarrierCode, String airport,
                            String customNo, int bookingDate);

    boolean isEffectivePSL(String marketingCarrierCode, String airport,
                           String customNo, int bookingDate);

    boolean isEffectivePSV(String marketingCarrierCode, String airport,
                           String customNo, int bookingDate);

}
