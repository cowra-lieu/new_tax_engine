package com.btw.tax_engine.computing_core.tax;

import com.btw.tax_engine.common.bean.Itinerary;
import com.btw.tax_engine.common.bean.SectorTaxFeeItem;
import com.btw.tax_engine.common.bean.TaxFeeItem;

import java.util.List;

public interface TaxCalculator {

    void execute(Itinerary itinerary, List<TaxFeeItem> feeList, List<SectorTaxFeeItem> sectorFeeList);

}
