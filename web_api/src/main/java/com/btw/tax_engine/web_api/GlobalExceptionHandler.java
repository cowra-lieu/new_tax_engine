package com.btw.tax_engine.web_api;

import com.btw.tax_engine.common.bean.DataRefreshResult;
import com.btw.tax_engine.common.exception.BizException;
import com.btw.tax_engine.common.bean.QueryResult;
import com.btw.tax_engine.common.exception.DataRefreshException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * A global handler for any uncaught exception at runtime.
 *
 * @author Cowra Lieu
 * @since 0.1-SNAPSHOT
 */

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BizException.class)
    public QueryResult handler(BizException e) {
        log.error("{}", e.rawItinerary);
        QueryResult queryResult = new QueryResult();
        queryResult.returnCode = e.code;
        queryResult.returnMsg = e.getMessage();
        return queryResult;
    }

    @ExceptionHandler(DataRefreshException.class)
    public DataRefreshResult handler(DataRefreshException e) {
        log.error(e.getMessage(), e);
        return new DataRefreshResult(false, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public QueryResult handler(Exception e) {
        if (e.getCause() instanceof BizException) {
            log.error(e.getMessage()+" - {}", ((BizException)e.getCause()).rawItinerary, e);
        } else {
            log.error(e.getMessage(), e);
        }
        QueryResult queryResult = new QueryResult();
        queryResult.returnCode = ExecStatus.ERROR.code;
        String rawMsg = e.getMessage();
        queryResult.returnMsg = String.format("%s(%s)", ExecStatus.ERROR.msg, rawMsg);
        return queryResult;
    }


}
