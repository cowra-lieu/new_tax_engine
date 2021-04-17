package com.btw.tax_engine.computing_core.tax.filter;

import com.btw.tax_engine.common.bean.TaxRule;
import com.btw.tax_engine.computing_core.tax.TaxCalculatorImpl;
import org.springframework.stereotype.Service;

import java.time.Year;

@Service
public class EasyFilter1 {

    public boolean check(TaxRule k, TaxCalculatorImpl.EasyInfo ei) {
        return (null == k.tax_Point_Loc1_Transfer_Type || k.loc1TT == ei.att)
                && (null != k.tax_Point_Tag && k.tpt == ei.atg)
                && ( 'E' == k.eul ? k.sdl >= ei.abd : k.tc >= ei.abd )
                && (null == k.travel_Dates_Tag || tax_travel_date(ei.afb, ei.asb, k));
    }

    private static boolean tax_travel_date(int afBegin, int asBegin, TaxRule k) {
        boolean result;
        int first;
        int last;
        if (k.travel_Dates_First.length() == 6) {
            first = k.tdf;
            last = k.tdl;
        } else {
            int year = (Year.now().getValue() % 2000) * 10000;
            first = year + k.tdf;
            last = year + k.tdl + ((k.tdf > k.tdl) ? 10000 : 0);
        }
        if ('T' == k.tdt) {
            result = (asBegin >= first && asBegin <= last);
        } else {
            result = (afBegin >= first && afBegin <= last);
        }
        return result;
    }

}
