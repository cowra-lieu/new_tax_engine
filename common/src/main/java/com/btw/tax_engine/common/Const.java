package com.btw.tax_engine.common;

import java.util.Arrays;
import java.util.List;

public interface Const {


    byte NO_EXIST = (byte)-1;

    byte USE_LIMIT_E = (byte)0;
    byte USE_LIMIT_EC = (byte)1;
    byte USE_LIMIT_ER = (byte)2;

    long INF_DAY = DEU.parse_y4M2d2("20991231").getTime();

    byte IS_INT = (byte)'I';
    byte IS_DOM = (byte)'D';

    byte REQUIRE_LOC1_IS_JOURNEY_ORIGIN = (byte)'A';
    byte MATCH_PER_PORTION = (byte)'P';

    byte SECTOR_PORTION_FROM_LOC1 = (byte)'1';
    byte SECTOR_PORTION_TO_LOC1 = (byte)'2';

    byte IS_ONE_WAY = (byte)'O';
    byte IS_ROUND_TRIP = (byte)'R';

    byte LOC_TYPE_AIRPORT = (byte)'P';
    byte LOC_TYPE_CITY = (byte)'C';
    byte LOC_TYPE_NATION = (byte)'N';
    byte LOC_TYPE_ZONE = (byte)'Z';
    byte LOC_TYPE_AREA = (byte)'A';
    byte LOC_TYPE_USER_DEFINE = (byte)'U';

    byte VIA_STOPOVER = (byte)'S';
    byte VIA_CONNECTION = (byte)'C';

    byte SECTOR_PORTION_DAY_UNIT = (byte)'D';
    byte SECTOR_PORTION_MONTH_UNIT = (byte)'M';

    byte FEE_APPLICATION_PER_DIRECTION = (byte)'1';
    byte FEE_APPLICATION_PER_JOURNEY = (byte)'2';

    String PJL = "PJL";
    String PSL = "PSL";
    String PSV = "PSV";

    String YQ = "YQ";
    String YR = "YR";

    String EIGHT_ZERO = "00000000";
    char NC = (char)0;
    String NULL_183_CXR = "..";
    String AREA_STR = "PCSNZA";

    int NATION = 4;


    int AS_CITY = 7;
    int AS_NATION = 4;
    int AS_ZONE = 2;
    int AS_AREA = 0;

    String US = "US";

    String DEFAULT_CXR = "MU";

    String INF = "INF";
    String COLON = ":";

    String MSG_S1_CT = "s1_ct";
    String MSG_X1_CT = "x1_ct";
    String MSG_PFC = "pfc";
    String MSG_ICER = "icer";

    String TOPIC_CORE_UDPATE = "core_data:main";

    List<Object> ICER_KEY_LIST = Arrays.asList("ICER_1", "ICER_2", "ICER_3");
}
