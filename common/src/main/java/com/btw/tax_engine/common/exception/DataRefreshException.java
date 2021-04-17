package com.btw.tax_engine.common.exception;

public class DataRefreshException extends RuntimeException {

    public DataRefreshException(Throwable cause) {
        super(cause.getMessage());
        this.setStackTrace(cause.getStackTrace());
    }

    public DataRefreshException(String msg) {
        super(msg);
    }

}
