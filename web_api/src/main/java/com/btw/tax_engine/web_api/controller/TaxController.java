package com.btw.tax_engine.web_api.controller;

import com.btw.tax_engine.common.bean.Itinerary;
import com.btw.tax_engine.common.bean.QueryResult;
import com.btw.tax_engine.web_api.service.TaxYQQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v3/api")
public class TaxController {

    private TaxYQQueryService taxYQQueryService;

    @CrossOrigin
    @RequestMapping(value = "/tax", method = {RequestMethod.GET, RequestMethod.POST})
    public QueryResult doQuery4tax(@RequestBody Itinerary itinerary) {
        return taxYQQueryService.doSyncQuery4Tax(itinerary);
    }

    @CrossOrigin
    @RequestMapping(value = "/yq", method = {RequestMethod.GET, RequestMethod.POST})
    public QueryResult doQuery4yq(@RequestBody Itinerary itinerary) {
        return taxYQQueryService.doSyncQuery4YQ(itinerary);
    }

    @CrossOrigin
    @RequestMapping(value = "/taxyq", method = {RequestMethod.GET, RequestMethod.POST})
    public QueryResult doQuery(@RequestBody Itinerary itinerary) {
        return taxYQQueryService.doSyncQuery(itinerary);
    }

    @CrossOrigin
    @RequestMapping(value = "/sector/taxyq", method = {RequestMethod.GET, RequestMethod.POST})
    public QueryResult doSectorQuery(@RequestBody Itinerary itinerary) {
        return taxYQQueryService.doSyncSectorQuery(itinerary);
    }

    @Autowired
    public void setTaxYQQueryService(TaxYQQueryService taxYQQueryService) {
        this.taxYQQueryService = taxYQQueryService;
    }

}
