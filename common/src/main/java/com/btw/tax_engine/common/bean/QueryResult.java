package com.btw.tax_engine.common.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * A plain old java bean for mapping HTTP response body in JSON.
 *
 * @author Cowra Lieu
 * @since 0.1-SNAPSHOT
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class QueryResult implements Serializable {

    public TaxFeeItem[] taxes;
    public String returnCode;
    public String returnMsg;

    public QueryResult() {}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QueryResult that = (QueryResult) o;
        return Arrays.equals(taxes, that.taxes) &&
                Objects.equals(returnCode, that.returnCode) &&
                Objects.equals(returnMsg, that.returnMsg);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(returnCode, returnMsg);
        result = 31 * result + Arrays.hashCode(taxes);
        return result;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", QueryResult.class.getSimpleName() + "[", "]")
                .add("taxes=" + Arrays.toString(taxes))
                .add("returnCode='" + returnCode + "'")
                .add("returnMsg='" + returnMsg + "'")
                .toString();
    }
}
