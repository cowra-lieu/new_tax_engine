package com.btw.tax_engine.computing_core.tax.filter;

import com.btw.tax_engine.common.SU;
import com.btw.tax_engine.quick_data_access.T183Repo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.btw.tax_engine.common.Const.EIGHT_ZERO;
import static com.btw.tax_engine.common.Const.NULL_183_CXR;

@Service
public class T183Filter {

    private T183Repo t183Repo;

    @Autowired
    public void setT183Repo(T183Repo t183Repo) {
        this.t183Repo = t183Repo;
    }

    public boolean check(String tblNo, String tcxr) {
        if (EIGHT_ZERO.equals(tblNo)) {
            return true;
        }
        boolean result = false;
        String value = t183Repo.getRawValue(tblNo);
        if (!SU.blk(value)) {
            result = value.contains(NULL_183_CXR) || value.contains(tcxr);
        }
        return result;
    }

}
