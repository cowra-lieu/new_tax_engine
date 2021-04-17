package com.btw.tax_engine.computing_core;

import com.btw.tax_engine.common.bean.Itinerary;
import com.btw.tax_engine.common.bean.TaxRule;

public interface AnalysisRouteService {

    boolean withSameCity(String airportCode1, String airportCode2);

    boolean withSameCountry(String airportCode1, String airportCode2);

    void analyzeIntlDomAndOwRt(Itinerary itinerary);

    void analyzeTurnaround(Itinerary itinerary);

    double getTPM(String fromCode, String toCode);

    void analyzeFurthestStopoverOrTicketedPoint(Itinerary itinerary, boolean stopoverExist);

    boolean findFurthestPoint(Itinerary itinerary, boolean domLine, boolean requireStopover);

    boolean checkLocation(byte locationType, String location, String code,
                          String partition, String carrier, int bookingDate);

    boolean airport_area_matching(String code, String atype, String avalue);

    boolean us_special_process(TaxRule k, Itinerary itinerary);

}
