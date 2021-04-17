package com.btw.tax_engine.computing_core.tax.filter;

import com.btw.tax_engine.common.bean.Itinerary;
import com.btw.tax_engine.common.bean.TaxRule;
import org.springframework.stereotype.Service;

@Service
public class EasyFilter0 {

    public boolean check(TaxRule k, Itinerary itinerary) {
        return  (null == k.point_Of_Sale_Info || k.point_Of_Sale_Info.equals(itinerary.spoint))
                && (null == k.p_Of_Ticketing_Geo_Spec_Info || k.p_Of_Ticketing_Geo_Spec_Info.equals(itinerary.tpoint))
                && (null == k.currency_Of_Sale || k.currency_Of_Sale.equals(itinerary.scurr))
                && (null == k.rtn_To_Orig || k.rto == itinerary.rtn_to_orig);
    }
}
