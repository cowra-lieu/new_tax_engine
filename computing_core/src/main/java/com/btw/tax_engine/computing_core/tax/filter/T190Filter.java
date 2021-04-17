package com.btw.tax_engine.computing_core.tax.filter;

import static com.btw.tax_engine.common.SU.*;
import com.btw.tax_engine.quick_data_access.T190Repo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.btw.tax_engine.common.Const.EIGHT_ZERO;

@Service
public class T190Filter {

    private T190Repo t190Repo;

    @Autowired
    public void setT190Repo(T190Repo t190Repo) {
        this.t190Repo = t190Repo;
    }

    public boolean check(String tblNo, String tcxr) {
        if (EIGHT_ZERO.equals(tblNo)) {
            return true;
        }
        boolean result = false;
        String value = t190Repo.getRawValue(tblNo);
        if (!blk(value)) {
            int n = 0;
            String sec;
            String s0;
            while (n < Integer.MAX_VALUE) {
                sec = nthsec(value, ',', n++);
                if (blk(sec)) {
                    break;
                }
                s0 = nthsec(sec, 0);
                if (s0.length() == 0) {
                    if (tcxr.equals(nthsec(sec, 1)) || rnthsec(sec, 0).contains(tcxr)) {
                        result = true;
                        break;
                    }
                } else if ('X' == s0.charAt(0)) {
                    if (!tcxr.equals(nthsec(sec, 1)) || !rnthsec(sec, 0).contains(tcxr)) {
                        result = true;
                        break;
                    }
                }
            }
        }
        return result;
    }

}
