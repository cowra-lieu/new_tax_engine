package com.btw.tax_engine.common.bean;

import java.util.Objects;
import java.util.StringJoiner;

public class DataRefreshResult {

    public boolean success;
    public String message;

    public DataRefreshResult(boolean success, String msg) {
        this.success = success;
        this.message = msg;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataRefreshResult that = (DataRefreshResult) o;
        return success == that.success &&
                Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(success, message);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", DataRefreshResult.class.getSimpleName() + "[", "]")
                .add("success=" + success)
                .add("message='" + message + "'")
                .toString();
    }
}
