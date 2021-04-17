package com.btw.tax_engine.computing_core.yqyr.filter;

import com.btw.tax_engine.common.bean.Itinerary;
import com.btw.tax_engine.common.bean.YqYrRule;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

import static com.btw.tax_engine.common.Const.*;

@Service
public class EasyRuleFilter {

    public static final Set<String> PASSENGER_TYPE_SET = new HashSet<>();
    static {
        PASSENGER_TYPE_SET.add("ADTADT");
        PASSENGER_TYPE_SET.add("CNNCHD");
        PASSENGER_TYPE_SET.add("CHDCHD");
        PASSENGER_TYPE_SET.add("INFINF");
        PASSENGER_TYPE_SET.add("CNNINF");
        PASSENGER_TYPE_SET.add("INSINS");
        PASSENGER_TYPE_SET.add("UNNUNN");
    }

    public boolean doFilter(Itinerary itinerary, YqYrRule rule) {
        return (null == rule.rtn_to_orig || rule.rto == itinerary.rtn_to_orig)
                &&
                ((rule.travel_eff <= itinerary.tdate || INF_DAY == itinerary.tdate)
                        && rule.travel_disc >= itinerary.tdate)
                &&
                (null == rule.psgr
                        || PASSENGER_TYPE_SET.contains(itinerary.passenger.type + rule.psgr))
                &&
                (rule.use_limit != USE_LIMIT_E
                        || (rule.ticket_first <= itinerary.atpco_bdate || INF_DAY == rule.ticket_first))
                &&
                (itinerary.atpco_bdate <=
                        ((rule.use_limit == USE_LIMIT_EC
                                || rule.use_limit == USE_LIMIT_ER) ? rule.travel_d : rule.ticket_last))
                &&
                (null == rule.point_of_sale_geographic_l || rule.point_of_sale_geographic_l.equals(itinerary.spoint))
                &&
                (null == rule.point_of_sale_code ||
                        (
                                ("T".equals(rule.point_of_sale_code) || "I".equals(rule.point_of_sale_code))
                                        &&
                                        (itinerary.spoint.equals(rule.point_of_sale_code_value))
                        )
                );
    }

}
