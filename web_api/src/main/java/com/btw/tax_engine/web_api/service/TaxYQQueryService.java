package com.btw.tax_engine.web_api.service;

import com.btw.tax_engine.common.bean.Itinerary;
import com.btw.tax_engine.common.bean.QueryResult;

import java.util.concurrent.CompletableFuture;

public interface TaxYQQueryService {

//    CompletableFuture<QueryResult> doQuery(Itinerary itinerary);
//    CompletableFuture<QueryResult> doSectorQuery(Itinerary itinerary);
//    CompletableFuture<QueryResult> doQuery4YQ(Itinerary itinerary);
//    CompletableFuture<QueryResult> doQuery4Tax(Itinerary itinerary);

    QueryResult doSyncQuery(Itinerary itinerary);
    QueryResult doSyncSectorQuery(Itinerary itinerary);
    QueryResult doSyncQuery4YQ(Itinerary itinerary);
    QueryResult doSyncQuery4Tax(Itinerary itinerary);

}
