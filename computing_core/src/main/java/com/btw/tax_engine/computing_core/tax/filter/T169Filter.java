package com.btw.tax_engine.computing_core.tax.filter;

import com.btw.tax_engine.common.Const;
import com.btw.tax_engine.common.DAU;
import com.btw.tax_engine.common.SU;
import com.btw.tax_engine.common.bean.Passenger;
import com.btw.tax_engine.computing_core.yqyr.filter.EasyRuleFilter;
import com.btw.tax_engine.quick_data_access.AirportRepo;
import com.btw.tax_engine.quick_data_access.T169Repo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.btw.tax_engine.common.Const.EIGHT_ZERO;
import static com.btw.tax_engine.common.SU.*;

@Service
public class T169Filter {

    private AirportRepo airportDao;
    private T169Repo t169Repo;

    @Autowired
    public void setAirportDao(AirportRepo airportDao) {
        this.airportDao = airportDao;
    }
    @Autowired
    public void setT169Repo(T169Repo t169Repo) {
        this.t169Repo = t169Repo;
    }

    public boolean check(String tblNo, Passenger passenger) {
        if (EIGHT_ZERO.equals(tblNo)) {
            return true;
        }
        boolean result = false;
        String value = t169Repo.getRawValue(tblNo);
        if (!SU.blk(value)) {
            int age_min;
            int age_max;
            int n = 0;
            String sec;
            String tmp;
            while (n < Integer.MAX_VALUE) {
                sec = nthsec(value, ',', n++);
                if (blk(sec)) {
                    break;
                }
                age_min = (tmp=rnthsec(sec, 1)).length() > 0 ? Integer.parseInt(tmp) : 0;
                age_max = (tmp=rnthsec(sec, 0)).length() > 0 ? Integer.parseInt(tmp) : 99;
                if (
                        (age_min <= passenger.getAge() && age_max>=passenger.age)
                        &&
                        ((tmp=nthsec(sec, 0)).length() == 0 || tmp.equals(passenger.id))
                        &&
                        ((tmp=rnthsec(sec, 2)).length() == 0 ||
                                EasyRuleFilter.PASSENGER_TYPE_SET.contains(tmp+passenger.type))
                        &&
                        ((tmp=nthsec(sec, 1)).length() == 0 || standard_area_comparison(String.valueOf(passenger.rtype),
                                passenger.region, nthsec(sec, 2), tmp))
                ) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    private boolean standard_area_comparison(String psgRType, String psgRegion,
                                             String specRType, String specRegion) {
        boolean result = false;
        if (SU.eq(psgRType, specRType) && SU.eq(psgRegion, specRegion)) {
            result = true;
        } else if (Const.AREA_STR.indexOf(psgRType) <= Const.AREA_STR.indexOf(specRType)) {
            char c = DAU.c(psgRType);
            switch (c) {
                case 'P':
                    result = p2a(psgRegion, specRType, specRegion);
                    break;
                case 'C':
                    result = c2a(psgRegion, specRType, specRegion);
                    break;
                case 'S':
                    result = s2a(psgRegion, specRType, specRegion);
                    break;
                case 'N':
                    result = n2a(psgRegion, specRType, specRegion);
                    break;
                case 'Z':
                    result = z2a(psgRegion, specRType, specRegion);
                    break;
            }
        }
        return result;
    }

    private boolean z2a(String zone, String atype, String avalue) {
        boolean result = false;
        String tmp;
//        String[] as;
        if ("A".equals(atype)) {
            tmp = t169Repo.z2a(zone);
            if (!SU.blk(tmp)) {
                /*
                as = tmp.split(",");
                for (String a:as) {
                    if (avalue.equals(a)) {
                        result = true;
                        break;
                    }
                }
                 */
                int n = 0;
                String sec;
                while (n < Integer.MAX_VALUE) {
                    sec = nthsec(tmp, ',', n++);
                    if (blk(sec)) {
                        break;
                    }
                    if (avalue.equals(sec)) {
                        result = true;
                        break;
                    }
                }
            }
        }
        return result;
    }

    private boolean n2a(String nation, String atype, String avalue) {
        boolean result = false;
        String tmp;
        if ("Z".equals(atype)){
            tmp = t169Repo.n2a(nation);
            if (!SU.blk(tmp)) {
                result = nthsec(tmp, 0).equals(avalue);
            }
        } else if ("A".equals(atype)) {
            tmp = t169Repo.n2a(nation);
            if (!SU.blk(tmp)) {
                result = rnthsec(tmp, 0).equals(avalue);
            }
        }
        return result;
    }

    private boolean s2a(String state, String atype, String avalue) {
        boolean result = false;
        String tmp;
        char c = DAU.c(atype);
        switch (c) {
            case 'N':
                tmp = t169Repo.ss2a(state);
                if (!SU.blk(tmp)) {
                    result = avalue.equals(nthsec(tmp, 1));
                }
                if (!result) {
                    tmp = t169Repo.s2a(state);
                    if (!SU.blk(tmp)) {
                        result = avalue.equals(nthsec(tmp, 0));
                    }
                }
                break;
            case 'Z':
                tmp = t169Repo.ss2a(state);
                if (!SU.blk(tmp)) {
                    result = avalue.equals(rnthsec(tmp, 1));
                }
                if (!result) {
                    tmp = t169Repo.s2a(state);
                    if (!SU.blk(tmp)) {
                        result = avalue.equals(nthsec(tmp, 1));
                    }
                }
                break;
            case 'A':
                tmp = t169Repo.ss2a(state);
                if (!SU.blk(tmp)) {
                    result = avalue.equals(rnthsec(tmp, 0));
                }
                if (!result) {
                    tmp = t169Repo.s2a(state);
                    if (!SU.blk(tmp)) {
                        result = avalue.equals(rnthsec(tmp, 0));
                    }
                }
                break;
        }
        /*
        if ("N".equals(atype)) {
            tmp = t169Repo.ss2a(state);
            if (!SU.blk(tmp)) {
                result = avalue.equals(nthSec(tmp, 1));
            }
            if (!result) {
                tmp = t169Repo.s2a(state);
                if (!SU.blk(tmp)) {
                    result = avalue.equals(nthSec(tmp, 0));
                }
            }
        } else if ("Z".equals(atype)) {
            tmp = t169Repo.ss2a(state);
            if (!SU.blk(tmp)) {
                result = avalue.equals(nthSec(tmp, 2));
            }
            if (!result) {
                tmp = t169Repo.s2a(state);
                if (!SU.blk(tmp)) {
                    result = avalue.equals(nthSec(tmp, 1));
                }
            }
        } else if ("A".equals(atype)) {
            tmp = t169Repo.ss2a(state);
            if (!SU.blk(tmp)) {
                result = avalue.equals(nthSec(tmp, 3));
            }
            if (!result) {
                tmp = t169Repo.s2a(state);
                if (!SU.blk(tmp)) {
                    result = avalue.equals(nthSec(tmp, 2));
                }
            }
        }
         */
        return result;
    }

    private boolean c2a(String city, String atype, String avalue) {
        boolean result = false;
        String tmp;
        char c = DAU.c(atype);
        switch (c) {
            case 'S':
                tmp = t169Repo.c2ss(city);
                if (!SU.blk(tmp)) {
                    result = avalue.equals(nthsec(tmp, 0)) || rnthsec(tmp, 0).contains(avalue);
                }
                break;
            case 'N':
                tmp = t169Repo.c2n(city);
                if (!SU.blk(tmp)) {
                    result = tmp.contains(avalue);
                }
                break;
            case 'Z':
                tmp = t169Repo.c2n(city);
                if (!blk(tmp)) {
                    int n = 0;
                    String sec;
                    while (n < Integer.MAX_VALUE) {
                        sec = nthsec(tmp, ',', n++);
                        if (blk(sec)) {
                            break;
                        }
                        if (nthsec(t169Repo.n2a(sec), 0).equals(avalue)) {
                            result = true;
                            break;
                        }
                    }
                }
                break;
            case 'A':
                tmp = t169Repo.c2n(city);
                if (!blk(tmp)) {
                    int n = 0;
                    String sec;
                    while (n < Integer.MAX_VALUE) {
                        sec = nthsec(tmp, ',', n++);
                        if (blk(sec)) {
                            break;
                        }
                        if (nthsec(t169Repo.n2a(sec), 1).equals(avalue)) {
                            result = true;
                            break;
                        }
                    }
                }
                break;
        }
        /*
        if ("S".equals(atype)) {
            tmp = t169Repo.c2ss(city);
            if (!SU.blk(tmp)) {
                result = avalue.equals(nthsec(tmp, 0)) || rnthsec(tmp, 0).contains(avalue);
            }
        } else if ("N".equals(atype)) {
            tmp = t169Repo.c2n(city);
            if (!SU.blk(tmp)) {
                result = tmp.contains(avalue);
            }
        } else if ("Z".equals(atype)) {
            tmp = t169Repo.c2n(city);
            String[] ns = tmp.split(",");
            for (String n:ns) {
                tmp = t169Repo.n2a(n);
                if (nthSec(tmp, 0).equals(avalue)) {
                    result = true;
                    break;
                }
            }
        } else if ("A".equals(atype)) {
            tmp = t169Repo.c2n(city);
            String[] ns = tmp.split(",");
            for (String n:ns) {
                tmp = t169Repo.n2a(n);
                if (nthSec(tmp, 1).equals(avalue)) {
                    result = true;
                    break;
                }
            }
        }
         */
        return result;
    }

    private boolean p2a(String airport, String atype, String avalue) {
        boolean result = false;
        String info = airportDao.getRawValue(airport);
        //a:sa:z:sz:n:s:ss:city:scity:u50
        char c = DAU.c(atype);
        switch (c) {
            case 'C':
                result = rnthsec(info, 2).equals(avalue);
                break;
            case 'S':
                result = rnthsec(info, 4).equals(avalue) || rnthsec(info, 3).equals(avalue);
                break;
            case 'N':
                result = nthsec(info, 4).equals(avalue);
                break;
            case 'Z':
                result = nthsec(info, 2).equals(avalue);
                break;
            case 'A':
                result = nthsec(info, 0).equals(avalue);
                break;
        }
        /*
        if ("C".equals(atype)) {
            result = nthSec(info, 7).equals(avalue);
        } else if ("S".equals(atype)) {
            result = nthSec(info, 5).equals(avalue) || nthSec(info, 6).equals(avalue);
        } else if ("N".equals(atype)) {
            result = nthSec(info, 4).equals(avalue);
        } else if ("Z".equals(atype)) {
            result = nthSec(info, 2).equals(avalue);
        } else if ("A".equals(atype)) {
            result = nthSec(info, 0).equals(avalue);
        }
        */
        return result;
    }

}
