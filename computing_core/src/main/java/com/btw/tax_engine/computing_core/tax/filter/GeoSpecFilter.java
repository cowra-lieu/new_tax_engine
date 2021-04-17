package com.btw.tax_engine.computing_core.tax.filter;

import com.btw.tax_engine.common.bean.Itinerary;
import com.btw.tax_engine.common.bean.Sector;
import com.btw.tax_engine.computing_core.AnalysisRouteService;
import com.btw.tax_engine.quick_data_access.TAirportCustRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GeoSpecFilter {

    private TAirportCustRepo airportCustRepo;
    private AnalysisRouteService routeService;

    @Autowired
    public void setAirportCustRepo(TAirportCustRepo airportCustRepo) {
        this.airportCustRepo = airportCustRepo;
    }

    @Autowired
    public void setRouteService(AnalysisRouteService routeService) {
        this.routeService = routeService;
    }

    boolean check_include(String value, String type, Itinerary itinerary) {
        if (value == null) {
            return true;
        }
        boolean result = false;
        String airportCode;
        for (Sector s : itinerary.sectors) {
            for (int i=0; i<2; i++) {
                airportCode = i==0 ? s.from : s.to;
                if ("U".equals(type)) {
                    if (airportCustRepo.isMemberOfPJV(value +airportCode)) {
                        result = true;
                        break;
                    }
                } else if (routeService.airport_area_matching(airportCode, type, value)) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    boolean check_wholly(String value, String type, Itinerary itinerary) {
        if (value == null) {
            return true;
        }
        boolean result = true;
        String airportCode;
        for (Sector s : itinerary.sectors) {
            for (int i=0; i<2; i++) {
                airportCode = i==0 ? s.from : s.to;
                if ("U".equals(type)) {
                    if (!airportCustRepo.isMemberOfPJW(value +airportCode)) {
                        result = false;
                        break;
                    }
                } else if (!routeService.airport_area_matching(airportCode, type, value)) {
                    result = false;
                    break;
                }
            }
        }
        return result;
    }

    boolean check_loc1(String loc1Info, String apInfo, String apc) {
        return (null == loc1Info) || loc1Info.equals(apc) ||
                apInfo.startsWith(loc1Info + ":") ||
                apInfo.endsWith(":" + loc1Info) ||
                apInfo.contains(":" + loc1Info + ":") ||
                airportCustRepo.isMemberOfPT1(loc1Info + apc);
    }

}
