package com.btw.tax_engine.web_api;

/**
 * An enum for the execution status of stored procedure.
 *
 * @author Cowra Lieu
 * @since 0.1-SNAPSHOT
 */

public enum ExecStatus {

    SUCCESS("01001", "Success"),
    AUTH_FAIL("02001", "User name or password is not correct"),
    INVALID_PARAMS("03001", "One or more request parameters are invalid"),
    AIRPORT_ERROR("03001", "One or more airports don't exist"),
    ERROR("04001", "Error");

    public String code;
    public String msg;

    ExecStatus(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

}
