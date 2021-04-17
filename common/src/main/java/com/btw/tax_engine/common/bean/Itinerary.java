package com.btw.tax_engine.common.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.StringJoiner;

import static com.btw.tax_engine.common.DEU.i6;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Itinerary {

    public User user = new User();
    public char stype;  // N: 普通查询，不记日志；Y：出票，记日志。
    public String ono; // order_no
    public String tno; // ticket_no

    public Date bdate = new Date();   // 用户提供，否则取系统时间
    public String scurr = "CNY";    // sale_currency, 用户提供，否则取 “CNY”
    public String spoint = "CN";   // sale_point, 用户提供，否则取“CN”
    public Passenger passenger = new Passenger();
    public String tpoint = "CN"; // ticketing_point 出票点国家码
    public String tcxr = "MU";

    public String fbase;
    public Fee fee = new Fee();

    public Sector[] sectors;

    public String lang = "en";

    @JsonIgnore
    public int turnaroundNo = -1;   // turnaround 的航段编号，不是 journey turnaround，【注意】它是基于 1 的

    // route status --------------------------------------- begin >>>
    @JsonIgnore
    public long tdate;  // travel_date

    @JsonIgnore
    public long atpco_bdate;    // atpco_booking_date

    @JsonIgnore
    public boolean rtn_to_orig = false;

    @JsonIgnore
    public boolean is_intl = false; // 默认 国内

    @JsonIgnore
    public boolean is_ow = true;    // 默认 One Way
    // airport index in route.
    // 1: 0 segment arrival; 2: 1 segment departure;
    // 3: 1 segment arrival; 4: 2 segment departure;
    @JsonIgnore
    public int journey_turnaround = -1;    // 默认 -1，表示无 turnaround
    @JsonIgnore
    public String journey_turnaround_code;
    @JsonIgnore
    public boolean hasCheckedTurnaround;
    @JsonIgnore
    public String[] aps;
    @JsonIgnore
    public double yqyrFee;
    // route status --------------------------------------- end <<<

    /**
     * 仅当 route 为 Round Trip 且不含 stopover 的前提下可调用本方法。
     *
     * @return true if the route is symmetry; otherwise false.
     */
    @JsonIgnore
    public boolean is_symmetry() {
        int i = 0;
        int j = this.sectors.length - 1;
        Sector leftSector, rightSector;
        while (i < j) {
            leftSector = sectors[i];
            rightSector = sectors[j];
            if
            (
                (
                    !(i == 0 && is_intl)
                    && !leftSector.from.equals(rightSector.to)
                )
                || !leftSector.to.equals(rightSector.from)
            ) {
                break;
            }
            i += 1;
            j -= 1;
        }
        return i > j;
    }

    @JsonIgnore
    public String[] getAPs() {
        if (aps == null) {
            aps = new String[this.sectors.length << 1];
            int i = 0;
            for (Sector s : sectors) {
                aps[i++] = s.from;
                aps[i++] = s.to;
            }
        }
        return aps;
    }

    public String key() {
        if (this.sectors != null) {
            StringBuilder sb = new StringBuilder();
            for (Sector s : this.sectors) {
                sb.append(s.toString());
            }
            sb.append(i6(this.bdate));
            sb.append(this.passenger);
            sb.append(this.fbase);
            sb.append(this.fee);
            sb.append(this.scurr);
            return sb.toString();
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Itinerary itinerary = (Itinerary) o;
        return Objects.equals(bdate, itinerary.bdate) &&
                Objects.equals(scurr, itinerary.scurr) &&
                Objects.equals(spoint, itinerary.spoint) &&
                Objects.equals(passenger, itinerary.passenger) &&
                Objects.equals(tpoint, itinerary.tpoint) &&
                Objects.equals(tcxr, itinerary.tcxr) &&
                Objects.equals(fbase, itinerary.fbase) &&
                Objects.equals(fee, itinerary.fee) &&
                Arrays.equals(sectors, itinerary.sectors);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(bdate, scurr, spoint, passenger, tpoint, tcxr, fbase, fee);
        result = 31 * result + Arrays.hashCode(sectors);
        return result;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Itinerary.class.getSimpleName() + "[", "]")
                .add("user=" + user)
                .add("stype=" + stype)
                .add("ono='" + ono + "'")
                .add("tno='" + tno + "'")
                .add("bdate=" + bdate)
                .add("scurr='" + scurr + "'")
                .add("spoint='" + spoint + "'")
                .add("passenger=" + passenger)
                .add("tpoint='" + tpoint + "'")
                .add("tcxr='" + tcxr + "'")
                .add("fbase='" + fbase + "'")
                .add("fee=" + fee)
                .add("sectors=" + Arrays.toString(sectors))
                .toString();
    }
}
