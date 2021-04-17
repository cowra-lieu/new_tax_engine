package com.btw.tax_engine.computing_core.tax.filter;

import static com.btw.tax_engine.common.SU.*;
import com.btw.tax_engine.common.bean.Itinerary;
import com.btw.tax_engine.common.bean.Sector;
import com.btw.tax_engine.quick_data_access.T167Repo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.btw.tax_engine.common.Const.EIGHT_ZERO;

@Service
public class T167Filter {

    private T167Repo t167Repo;

    @Autowired
    public void setT167Repo(T167Repo t167Repo) {
        this.t167Repo = t167Repo;
    }

    public boolean check(String tblNo, Itinerary itinerary, int si) {
        if (EIGHT_ZERO.equals(tblNo)) {
            return true;
        }
        boolean result = false;
        Sector s = itinerary.sectors[si];
        String valueY = t167Repo.getRawValueY(tblNo);
        String valueX;
        if (!blk(valueY)) {
            int n = 0;
            int m;
            String infoY;
            String infoX;
            while (n < Integer.MAX_VALUE) {
                infoY = nthsec(valueY, ',', n++);
                if (blk(infoY)) {
                    break;
                }
                if (check167SubInfo(infoY, itinerary, s)) {
                    valueX = t167Repo.getRawValueX(tblNo);
                    if (blk(valueX)) {
                        result = true;
                        break;
                    } else {
                        boolean notFoundX = true;
                        m = 0;
                        while (m < Integer.MAX_VALUE) {
                            infoX = nthsec(valueX, ',', m++);
                            if (blk(infoX)) {
                                break;
                            }
                            if (check167SubInfo(infoX, itinerary, s)) {
                                notFoundX = false;
                                break;
                            }
                        }
                        if (notFoundX) {
                            result = true;
                            break;
                        }
                    }
                }
            }
        }
        return result;
    }

    private boolean check167SubInfo(String info, Itinerary itinerary, Sector sector) {
        String fbtd = nthsec(info, 0);
        String cabin = nthsec(info, 1);
        String rbd = rnthsec(info, 1);
        String eqp = rnthsec(info, 0);
        return (fbtd.length() == 0 || fbtd.equals(itinerary.fbase))
                &&
                (cabin.length() == 0 || cabin.charAt(0) == sector.cabin)
                &&
                (rbd.length() == 0 || rbd.indexOf(sector.clazz)>=0)
                &&
                (eqp.length() == 0 || eqp.equals(sector.ptype));
    }


}
