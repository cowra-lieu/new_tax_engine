package com.btw.tax_engine.computing_core.tax.filter;

import static com.btw.tax_engine.common.Const.*;
import static com.btw.tax_engine.common.SU.*;
import com.btw.tax_engine.computing_core.tax.TaxCalculatorImpl;
import com.btw.tax_engine.quick_data_access.T186Repo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class T186Filter {

    private T186Repo t186Repo;

    @Autowired
    public void setT186Repo(T186Repo t186Repo) {
        this.t186Repo = t186Repo;
    }

    public boolean check(String tblNo, TaxCalculatorImpl.Mofp inbound, TaxCalculatorImpl.Mofp outbound,
                         boolean isOutbound) {
        if (EIGHT_ZERO.equals(tblNo)) {
            return true;
        }
        boolean result = false;
        String value = t186Repo.getRawValue(tblNo);
        if (!blk(value)) {
            int n = 0;
            String sec;
            String inFno = inbound.f;
            String outFno = outbound.f;
            String tmcxr;
            String tocxr;
            String fno1 = null;
            String fno2;
            String mcxr = isOutbound ? outbound.m : inbound.m;
            String ocxr = isOutbound ? outbound.o : inbound.o;
            while (n < Integer.MAX_VALUE) {
                sec = nthsec(value, ',', n);
                if (sec == null) {
                    break;
                }
                if (
                        ( (tmcxr=nthsec(sec,0)).length()==0 || tmcxr.equals(mcxr) )
                                &&
                                ( (tocxr=nthsec(sec,1)).length()==0 || tocxr.equals(ocxr) )
                                && (
                                (
                                        (fno2 = rnthsec(sec, 0)).length() == 0
                                                &&
                                                (
                                                        (fno1 = rnthsec(sec, 1)).equals("****")
                                                                ||
                                                                (
                                                                        fno1.length() > 0
                                                                                &&
                                                                                (
                                                                                        ( isOutbound && (s2i(fno1) == s2i(outFno)) )
                                                                                                ||
                                                                                                ( !isOutbound && (s2i(fno1) == s2i(inFno)) )
                                                                                )
                                                                )
                                                )
                                )
                                        ||
                                        (
                                                fno2.length() > 0 && isaBoolean(isOutbound, sec, inFno, outFno, fno1, fno2)
                                        )
                        )
                ) {
                    result = true;
                    break;
                }
                n++;
            }

        }
        return result;
    }

    private boolean isaBoolean(boolean isOutbound, String sec, String inFno, String outFno, String fno1, String fno2) {
        if (isOutbound) {
            int ofn = s2i(outFno);
            int fn1 = s2i((null == fno1) ? rnthsec(sec, 1) : fno1);
            return ofn >= fn1 && ofn <= s2i(fno2);
        } else {
            int ifn = s2i(inFno);
            int fn1 = s2i((null == fno1) ? rnthsec(sec, 1) : fno1);
            return ifn >= fn1 && ifn <= s2i(fno2);
        }
    }

    private int s2i(String s)
    {
        String FNO_PREFIX = "^[A-Za-z]+";
        if (StringUtils.hasLength(s)) {
            return Integer.parseInt(s.replaceAll(FNO_PREFIX, ""));
        }
        return 0;
    }


}
