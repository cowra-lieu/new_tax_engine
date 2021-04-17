package com.btw.tax_engine.computing_core;

import com.btw.tax_engine.common.DEU;
import com.btw.tax_engine.common.bean.Itinerary;
import com.btw.tax_engine.common.bean.Passenger;
import com.btw.tax_engine.common.bean.Sector;
import com.btw.tax_engine.quick_data_access.CabinRepo;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.btw.tax_engine.common.DEU.i8;

public class TestUtil {

    private static final String SEP = ";";

    public static void readManyFromTxt(List<Itinerary> itis, String file, CabinRepo cabinRepo) {
        try (
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(Objects.requireNonNull(TestUtil.class.getResourceAsStream(file)))
                )
        ) {
            Itinerary iti = null;
            String line;
            int index = 0;

            String[] sectorArray = null;
            String[] departureDateArray = null;
            String[] arrivalDateArray = null;
            String[] classArray = null;
            String[] marketingCarrierArray = null;
            String passengerType = "ADT";
            String saleCurrency = "CNY";
            String spoint = "CN";
            String[] optCarrierArray = null;
            String[] mfnoArray = null;
            String[] ofnoArray = null;
            String[] ptypeArray = null;

            while((line = br.readLine())!=null){

                line = line.trim();
                if (line.length() == 0)
                    continue;

                if (line.startsWith("---")) {

                    if (sectorArray != null && iti != null) {
                        fillItinerary(iti, sectorArray, departureDateArray, arrivalDateArray,
                                classArray, marketingCarrierArray,
                                optCarrierArray, mfnoArray, ofnoArray, ptypeArray,
                                passengerType, saleCurrency, spoint, cabinRepo);
                        sectorArray = null;
                        departureDateArray = null;
                        arrivalDateArray = null;
                        classArray = null;
                        marketingCarrierArray = null;
                        passengerType = "ADT";
                        saleCurrency = "CNY";
                        spoint = "CN";
                        optCarrierArray = null;
                        mfnoArray = null;
                        ofnoArray = null;
                        ptypeArray = null;
                    }

                    iti = new Itinerary();
                    itis.add(iti);
                    index = 0;
                    continue;
                }

                if (index == 0) {
                    sectorArray = line.split(SEP);
                } else if (index == 1) {
                    departureDateArray = line.split(SEP);
                } else if (index == 2) {
                    arrivalDateArray = line.split(SEP);
                } else if (index == 3) {
                    classArray = line.split(SEP);
                } else if (index == 4) {
                    marketingCarrierArray = line.split(SEP);
                } else if (index == 5) {
                    passengerType = line;
                } else if (index == 6) {
                    saleCurrency = line;
                } else if (index == 7) {
                    optCarrierArray = line.split(SEP);
                } else if (index == 8) {
                    mfnoArray = line.split(SEP);
                } else if (index == 9) {
                    ofnoArray = line.split(SEP);
                } else if (index == 10) {
                    ptypeArray = line.split(SEP);
                } else if (index == 11) {
                    spoint = line;
                }
                index += 1;

            }

            if (sectorArray != null && iti != null) {
                fillItinerary(iti, sectorArray, departureDateArray, arrivalDateArray,
                        classArray, marketingCarrierArray,
                        optCarrierArray, mfnoArray, ofnoArray, ptypeArray,
                        passengerType, saleCurrency, spoint, cabinRepo);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void readYqYrFees(List<String[]> yqyrFeeList, String file)
    {
        try (
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(Objects.requireNonNull(TestUtil.class.getResourceAsStream(file)))
                )
        ) {
            String[] yqyrFees = new String[2];
            String line;
            int index = 0;

            while((line = br.readLine())!=null){

                line = line.trim();
                if (line.length() == 0)
                    continue;

                if (line.startsWith("---")) {
                    yqyrFees = new String[2];
                    yqyrFeeList.add(yqyrFees);
                    index = 0;
                    continue;
                }

                if (index == 0) {
                    yqyrFees[0] = line;
                } else if (index == 1) {
                    yqyrFees[1] = line;
                }
                index += 1;

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void readTaxFees(List<String[]> taxesList, String file)
    {
        try (
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(Objects.requireNonNull(TestUtil.class.getResourceAsStream(file)))
                )
        ) {
            String[] totalSectorFees = new String[2];
            String line;
            int index = 0;

            while((line = br.readLine())!=null){

                line = line.trim();
                if (line.length() == 0)
                    continue;

                if (line.startsWith("---")) {
                    totalSectorFees = new String[2];
                    taxesList.add(totalSectorFees);
                    index = 0;
                    continue;
                }

                if (index == 0) {
                    totalSectorFees[0] = line;
                } else if (index == 1) {
                    totalSectorFees[1] = line;
                }
                index += 1;

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Itinerary readOneFromTxt(String file, CabinRepo cabinRepo) {
        Itinerary itinerary = new Itinerary();
        try (
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(Objects.requireNonNull(TestUtil.class.getResourceAsStream(file)))
                )
        ) {
            String lineContent;
            int i = 0;
            String[] sectorArray = null;
            String[] departureDateArray = null;
            String[] arrivalDateArray = null;
            String[] classArray = null;
            String[] marketingCarrierArray = null;
            String passengerType = "ADT";
            String saleCurrency = "CNY";
            String[] optCarrierArray = null;
            String[] mfnoArray = null;
            String[] ofnoArray = null;
            String[] ptypeArray = null;
            String spoint = "CN";

            while((lineContent = br.readLine())!=null){
                if (i == 0) {
                    sectorArray = lineContent.trim().split(";");
                } else if (i == 1) {
                    departureDateArray = lineContent.trim().split(";");
                } else if (i == 2) {
                    arrivalDateArray = lineContent.trim().split(";");
                } else if (i == 3) {
                    classArray = lineContent.trim().split(";");
                } else if (i == 4) {
                    marketingCarrierArray = lineContent.trim().split(";");
                } else if (i == 5) {
                    passengerType = lineContent.trim();
                } else if (i == 6) {
                    saleCurrency = lineContent.trim();
                } else if (i == 7 && lineContent.trim().length()>0) {
                    optCarrierArray = lineContent.trim().split(";");
                } else if (i == 8) {
                    mfnoArray = lineContent.trim().split(";");
                } else if (i == 9) {
                    ofnoArray = lineContent.trim().split(";");
                } else if (i == 10) {
                    ptypeArray = lineContent.trim().split(";");
                } else if (i == 11) {
                    spoint = lineContent.trim();
                } else {
                    break;
                }
                i++;
            }
            if (sectorArray != null) {
                fillItinerary(itinerary, sectorArray, departureDateArray, arrivalDateArray,
                        classArray, marketingCarrierArray,
                        optCarrierArray, mfnoArray, ofnoArray, ptypeArray,
                        passengerType, saleCurrency, spoint, cabinRepo);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return itinerary;
    }

    private static void fillItinerary(Itinerary itinerary,
                                      String[] sectorArray,
                                      String[] departureDateArray,
                                      String[] arrivalDateArray,
                                      String[] classArray,
                                      String[] marketingCarrierArray,
                                      String[] optCarrierArray, String[] mfnoArray, String[] ofnoArray,
                                      String[] ptypeArray,
                                      String passengerType, String saleCurrency, String spoint,
                                      CabinRepo cabinRepo) {

        String[] airportCodeArray;

        Date today = new Date();
        DEU.truncDate(today);

        itinerary.sectors = new Sector[sectorArray.length];

        for (int i=0; i<sectorArray.length; i++) {
            Sector s = new Sector(i);
            s.begin = DEU.parse_y4M2d2_H2_m2(departureDateArray[i]);
            s.end = DEU.parse_y4M2d2_H2_m2(arrivalDateArray[i]);
            airportCodeArray = sectorArray[i].split("-");
            s.from = airportCodeArray[0];
            s.to = airportCodeArray[1];
            s.clazz = (classArray[i].charAt(0));
            s.mcxr = marketingCarrierArray[i];
            if (optCarrierArray != null) {
                s.ocxr = optCarrierArray[i];
            }
            if (mfnoArray != null) {
                s.mfno = mfnoArray[i];
            }
            if (ofnoArray != null) {
                s.ofno = ofnoArray[i];
            }
            if (ptypeArray != null) {
                s.ptype = ptypeArray[i];
            }
            s.cabin = cabinRepo.getCabin(s.mcxr, s.clazz, i8(today));

            itinerary.sectors[i] = s;
        }

        Passenger p = new Passenger();
        itinerary.passenger = p;
        p.type = passengerType;

        itinerary.scurr = saleCurrency;
        if (StringUtils.hasLength(spoint))
            itinerary.spoint = spoint;

        markStatusFromRoute(itinerary);
    }

    private static void markStatusFromRoute(Itinerary itinerary) {
        itinerary.atpco_bdate = itinerary.bdate.getTime();
        itinerary.tdate = itinerary.sectors[0].begin.getTime();
    }

}
