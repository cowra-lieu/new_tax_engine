package com.btw.tax_engine.computing_core.tax.filter;

import com.btw.tax_engine.common.bean.Itinerary;
import com.btw.tax_engine.common.bean.TaxRule;
import com.btw.tax_engine.computing_core.AnalysisRouteService;
import com.btw.tax_engine.computing_core.tax.TaxCalculatorImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SubTableFilter {

    private T190Filter t190Filter;
    private T186Filter t186Filter;
    private T167Filter t167Filter;
    private T169Filter t169Filter;
    private T183Filter t183Filter;

    private JLocFilter jLocFilter;
    private GeoSpecFilter geoSpecFilter;

    private AnalysisRouteService routeService;

    @Autowired
    public void setT190Filter(T190Filter t190Filter) {
        this.t190Filter = t190Filter;
    }

    @Autowired
    public void setT186Filter(T186Filter t186Filter) {
        this.t186Filter = t186Filter;
    }
    @Autowired
    public void setT167Filter(T167Filter t167Filter) {
        this.t167Filter = t167Filter;
    }
    @Autowired
    public void setT169Filter(T169Filter t169Filter) {
        this.t169Filter = t169Filter;
    }
    @Autowired
    public void setT183Filter(T183Filter t183Filter) {
        this.t183Filter = t183Filter;
    }
    @Autowired
    public void setjLocFilter(JLocFilter jLocFilter) {
        this.jLocFilter = jLocFilter;
    }
    @Autowired
    public void setGeoSpecFilter(GeoSpecFilter geoSpecFilter) {
        this.geoSpecFilter = geoSpecFilter;
    }

    @Autowired
    public void setRouteService(AnalysisRouteService routeService) {
        this.routeService = routeService;
    }

    public boolean check(TaxRule k, final Itinerary itinerary, TaxCalculatorImpl.SubFilterInfo sfi,
                         TaxCalculatorImpl.Mofp inbound, TaxCalculatorImpl.Mofp outbound) {

        return t190Filter.check(k.carrier_Appltable_No_190, itinerary.tcxr)
            && t186Filter.check(k.cxf_Or_Flt_Tbl_No_186_1, inbound, outbound, true)
            && t186Filter.check(k.cxf_Or_Flt_Tbl_No_186, inbound, outbound, false)
            && t167Filter.check(k.sector_Detail_Table_No_167, itinerary, sfi.si)
            && t169Filter.check(k.ptc_Table_169, itinerary.passenger)
            && t183Filter.check(k.security_Table_No_183, itinerary.tcxr)
            && jLocFilter.check1(k.jrny_Geo_Spec_Loc1_Info, itinerary)
            && jLocFilter.check2(k.jrny_Geo_Spec_Loc2_Info, k.jrny_Geo_Spec_Indicator, itinerary)
            && geoSpecFilter.check_include(k.jrny_Geo_Spec_Jo_In_Info, k.jrny_Geo_Spec_Jo_In_Type, itinerary)
            && geoSpecFilter.check_wholly(k.jrny_Geo_Spec_Trvl_In_Loc_Info, k.jrny_Geo_Spec_Trvl_In_Loc_Type, itinerary)
            && geoSpecFilter.check_loc1(k.tax_Point_Loc1_Info, sfi.apInfo, sfi.apc)
            && (
                    !"01".equals(k.tax_Processing_Appl_Tag)
                    ||
                    routeService.us_special_process(k, itinerary)
            );
    }

}
