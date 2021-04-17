package com.btw.tax_engine.computing_core.tax.filter;

import com.btw.tax_engine.common.bean.Itinerary;
import com.btw.tax_engine.computing_core.AnalysisRouteService;
import com.btw.tax_engine.quick_data_access.AirportRepo;
import com.btw.tax_engine.quick_data_access.TAirportCustRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.btw.tax_engine.common.Const.NO_EXIST;

@Service
public class JLocFilter {

    private AnalysisRouteService routeService;
    private AirportRepo airportDao;
    private TAirportCustRepo airportCustRepo;

    @Autowired
    public void setRouteService(AnalysisRouteService routeService) {
        this.routeService = routeService;
    }

    @Autowired
    public void setAirportDao(AirportRepo airportDao) {
        this.airportDao = airportDao;
    }
    @Autowired
    public void setAirportCustRepo(TAirportCustRepo airportCustRepo) {
        this.airportCustRepo = airportCustRepo;
    }

    boolean check1(String customNo, Itinerary itinerary) {
        if (customNo == null) {
            return true;
        }
        String from = itinerary.sectors[0].from;
        String originInfo = airportDao.getRawValue(from);
        return customNo.equals(from) || originInfo.contains(customNo) ||
                airportCustRepo.isMemberOfPJ1(customNo+from);
    }

    boolean check2(String customNo, String gi, Itinerary itinerary) {
        if (customNo == null) {
            return true;
        }
        boolean result = false;
        int lsi = itinerary.sectors.length - 1;
        String to = itinerary.sectors[lsi].to;
        if ("A".equals(gi) || !itinerary.rtn_to_orig) {
            String destInfo = airportDao.getRawValue(to);
            result = customNo.equals(to) || destInfo.contains(customNo) ||
                    airportCustRepo.isMemberOfPJ2(customNo+to);
        } else {
            if (gi == null) {
                routeService.analyzeTurnaround(itinerary);
                String tcode = itinerary.journey_turnaround == NO_EXIST ?
                         to : itinerary.journey_turnaround_code;
                String turnaroundInfo = airportDao.getRawValue(tcode);
                result = (customNo.equals(tcode) || turnaroundInfo.contains(customNo) ||
                        airportCustRepo.isMemberOfPJ2(customNo+tcode));
            }
        }
        return result;
    }

}
