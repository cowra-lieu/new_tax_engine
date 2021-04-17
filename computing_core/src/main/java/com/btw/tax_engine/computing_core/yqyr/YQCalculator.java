package com.btw.tax_engine.computing_core.yqyr;

import com.btw.tax_engine.common.bean.YqFeeItem;
import com.btw.tax_engine.common.bean.Itinerary;

import java.util.List;

public interface YQCalculator {

    void execute(Itinerary itinerary, List<YqFeeItem> yqFeeItemList, String taxName);

}
