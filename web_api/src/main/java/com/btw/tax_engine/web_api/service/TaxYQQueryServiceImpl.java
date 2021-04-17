package com.btw.tax_engine.web_api.service;

import com.btw.tax_engine.common.Const;
import com.btw.tax_engine.common.bean.*;
import com.btw.tax_engine.computing_core.XchangeService;
import com.btw.tax_engine.computing_core.tax.TaxCalculator;
import com.btw.tax_engine.computing_core.yqyr.YQCalculator;
import com.btw.tax_engine.web_api.ExecStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

//import org.springframework.scheduling.annotation.Async;
//import java.util.concurrent.CompletableFuture;

@Service
public class TaxYQQueryServiceImpl implements TaxYQQueryService {

    private YQCalculator yqCalculator;
    private TaxCalculator taxCalculator;

    private XchangeService xchangeService;

    @Autowired
    public void setYqCalculator(YQCalculator yqCalculator) {
        this.yqCalculator = yqCalculator;
    }
    @Autowired
    public void setTaxCalculator(TaxCalculator taxCalculator) {
        this.taxCalculator = taxCalculator;
    }
    @Autowired
    public void setXchangeService(XchangeService xchangeService) {
        this.xchangeService = xchangeService;
    }

    /*@Override
//    @Cacheable(value = "itineraryCache", key = "#p0.key()")
    @Async("ae")
    public CompletableFuture<QueryResult> doQuery(Itinerary itinerary) {
        QueryResult result = new QueryResult();
        return query(itinerary, false, result);
    }

    @Override
//    @Cacheable(value = "itinerarySectorCache", key = "#p0.key()")
    @Async("ae")
    public CompletableFuture<QueryResult> doSectorQuery(Itinerary itinerary) {
        QueryResult result = new QueryResult();
        return query(itinerary, true, result);
    }*/

    @Override
    @Cacheable(value = "itineraryCache", key = "#p0.key()")
    public QueryResult doSyncQuery(Itinerary itinerary) {
        QueryResult result = new QueryResult();
        sync_query(itinerary, false, result);
        return result;
    }

    @Override
    @Cacheable(value = "itinerarySectorCache", key = "#p0.key()")
    public QueryResult doSyncSectorQuery(Itinerary itinerary) {
        QueryResult result = new QueryResult();
        sync_query(itinerary, true, result);
        return result;
    }

    private void sync_query(Itinerary itinerary, boolean withSectorInfo, QueryResult result) {
        List<YqFeeItem> yqFeeList = new ArrayList<>();
//        long t0 = System.currentTimeMillis();
        yqCalculator.execute(itinerary, yqFeeList, Const.YQ);
        yqCalculator.execute(itinerary, yqFeeList, Const.YR);
        for (YqFeeItem y : yqFeeList) {
            itinerary.yqyrFee += y.amount;
        }
//        t0 = System.currentTimeMillis() - t0;
//        System.out.println("yqr cost: " + t0);
        List<TaxFeeItem> taxFeeList = new ArrayList<>();
//        t0 = System.currentTimeMillis();
        if (withSectorInfo) {
            List<SectorTaxFeeItem> sectorTaxFeeList = new ArrayList<>();
            taxCalculator.execute(itinerary, taxFeeList, sectorTaxFeeList);
            fillSectorResult(result, yqFeeList, sectorTaxFeeList, itinerary);
        } else {
            taxCalculator.execute(itinerary, taxFeeList, null);
            fillResult(result, yqFeeList, taxFeeList, itinerary);
        }
//        t0 = System.currentTimeMillis() - t0;
//        System.out.println("tax cost: " + t0);
    }

    /*private CompletableFuture<QueryResult> query(Itinerary itinerary, boolean withSectorInfo, QueryResult result) {
        sync_query(itinerary, withSectorInfo, result);
        return CompletableFuture.completedFuture(result);
    }

    @Override
    @Cacheable(value = "yqCache", key = "#p0.key()")
    @Async("ae")
    public CompletableFuture<QueryResult> doQuery4YQ(Itinerary itinerary) {
        QueryResult result = new QueryResult();
        List<YqFeeItem> yqFeeList = new ArrayList<>();
        yqCalculator.execute(itinerary, yqFeeList, Const.YQ);
        yqCalculator.execute(itinerary, yqFeeList, Const.YR);
        fillResult(result, yqFeeList, new ArrayList<>(), itinerary);
        return CompletableFuture.completedFuture(result);
    }

    @Override
    @Cacheable(value = "taxCache", key = "#p0.key()")
    @Async("ae")
    public CompletableFuture<QueryResult> doQuery4Tax(Itinerary itinerary) {
        QueryResult result = new QueryResult();
        List<TaxFeeItem> taxFeeList = new ArrayList<>();
        taxCalculator.execute(itinerary, taxFeeList, null);
        fillResult(result, null, taxFeeList, itinerary);
        return CompletableFuture.completedFuture(result);
    }*/

    @Override
    public QueryResult doSyncQuery4YQ(Itinerary itinerary) {
        QueryResult result = new QueryResult();
        List<YqFeeItem> yqFeeList = new ArrayList<>();
        yqCalculator.execute(itinerary, yqFeeList, Const.YQ);
        yqCalculator.execute(itinerary, yqFeeList, Const.YR);
        fillResult(result, yqFeeList, new ArrayList<>(), itinerary);
        return result;
    }

    @Override
    public QueryResult doSyncQuery4Tax(Itinerary itinerary) {
        QueryResult result = new QueryResult();
        List<TaxFeeItem> taxFeeList = new ArrayList<>();
        taxCalculator.execute(itinerary, taxFeeList, null);
        fillResult(result, null, taxFeeList, itinerary);
        return result;
    }

    private void fillResult(QueryResult result, List<YqFeeItem> yqFeeList, List<TaxFeeItem> taxFeeList,
                            Itinerary itinerary) {

        result.returnCode = ExecStatus.SUCCESS.code;
        result.returnMsg = ExecStatus.SUCCESS.msg;

        if (yqFeeList != null) {
            double[] yqyr = new double[]{0, 0};
            yqFeeList.forEach(yqFeeItem -> {
                if (yqFeeItem.name.equals(Const.YQ)) {
                    yqyr[0] += yqFeeItem.amount;
                } else {
                    yqyr[1] += yqFeeItem.amount;
                }
            });
            taxFeeList.add(
                    new TaxFeeItem(Const.YQ, xchangeService.round(yqyr[0], itinerary.scurr), itinerary.scurr, "")
            );
            taxFeeList.add(
                    new TaxFeeItem(Const.YR, xchangeService.round(yqyr[1], itinerary.scurr), itinerary.scurr, "")
            );
        }

        TaxFeeItem[] taxes = new TaxFeeItem[taxFeeList.size()];
        result.taxes = taxFeeList.toArray(taxes);
    }

    private void fillSectorResult(QueryResult result, List<YqFeeItem> yqFeeList, List<SectorTaxFeeItem> sectorFeeList,
                                  Itinerary itinerary) {

        result.returnCode = ExecStatus.SUCCESS.code;
        result.returnMsg = ExecStatus.SUCCESS.msg;

        if (yqFeeList != null) {
            yqFeeList.forEach(yqFeeItem ->
                sectorFeeList.add(new SectorTaxFeeItem(yqFeeItem.name, yqFeeItem.amount, itinerary.scurr, yqFeeItem.sectors))
            );
        }

        SectorTaxFeeItem[] taxes = new SectorTaxFeeItem[sectorFeeList.size()];
        result.taxes = sectorFeeList.toArray(taxes);

    }

}
