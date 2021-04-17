package com.btw.tax_engine.common.bean;

import com.btw.tax_engine.common.Const;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Date;
import java.util.Objects;
import java.util.StringJoiner;

import static com.btw.tax_engine.common.Const.NC;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Sector {

    private int seq;
    public String from;
    public String to;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone="GMT+8")
    public Date begin;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone="GMT+8")
    public Date end;
    public String mcxr = Const.DEFAULT_CXR;
    public String ocxr;
    public String mfno;
    public String ofno;
    public char cabin = NC;
    public char clazz = 'Y';
    public String ptype;

    @JsonIgnore
    public boolean departure_is_stopover = false;
    @JsonIgnore
    public boolean arrival_is_stopover = false;
    @JsonIgnore
    public boolean departure_to_origin_is_dom = false;
    @JsonIgnore
    public boolean arrival_to_origin_is_dom = false;

    @JsonIgnore
    public boolean isDom = false;

    public Sector(String fromCode, String toCode, Date fromDate, Date toDate) {
        this.from = fromCode;
        this.to = toCode;
        this.begin = fromDate;
        this.end = toDate;
    }

    public Sector(int i) {
        this.seq = i;
    }

    public Sector() {}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sector sector = (Sector) o;
        return seq == sector.seq &&
                cabin == sector.cabin &&
                clazz == sector.clazz &&
                Objects.equals(from, sector.from) &&
                Objects.equals(to, sector.to) &&
                Objects.equals(begin, sector.begin) &&
                Objects.equals(end, sector.end) &&
                Objects.equals(mcxr, sector.mcxr) &&
                Objects.equals(ocxr, sector.ocxr) &&
                Objects.equals(mfno, sector.mfno) &&
                Objects.equals(ofno, sector.ofno) &&
                Objects.equals(ptype, sector.ptype);
    }

    @Override
    public int hashCode() {
        return Objects.hash(seq, from, to, begin, end, mcxr, ocxr, mfno, ofno, cabin, clazz, ptype);
    }

    @Override
    public String toString() {
        return new StringJoiner(",", "[", "]")
                .add("seq=" + seq)
                .add("from='" + from + "'")
                .add("to='" + to + "'")
                .add("begin=" + begin)
                .add("end=" + end)
                .add("mcxr='" + mcxr + "'")
                .add("ocxr='" + ocxr + "'")
                .add("mfno='" + mfno + "'")
                .add("ofno='" + ofno + "'")
                .add("cabin=" + cabin)
                .add("clazz=" + clazz)
                .add("ptype='" + ptype + "'")
                .toString();
    }
}
