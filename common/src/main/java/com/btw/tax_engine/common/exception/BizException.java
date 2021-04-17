package com.btw.tax_engine.common.exception;

/**
 * A runtime exception for business of application.
 *
 * @author Cowra Lieu
 * @since 0.1-SNAPSHOT
 */

public class BizException extends RuntimeException {

    public String code;
    public String rawItinerary;

    public BizException(String msg, String code, String rawItinerary) {
        super(msg);
        this.code = code;
        this.rawItinerary = rawItinerary;
    }

}
