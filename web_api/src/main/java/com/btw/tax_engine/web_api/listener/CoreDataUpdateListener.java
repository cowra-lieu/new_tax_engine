package com.btw.tax_engine.web_api.listener;

import com.btw.tax_engine.common.SU;
import com.btw.tax_engine.common.bean.TaxRule;
import com.btw.tax_engine.common.bean.YqYrRule;
import com.btw.tax_engine.computing_core.tax.TaxRuleProvider;
import com.btw.tax_engine.computing_core.yqyr.YqRuleProvider;
import com.btw.tax_engine.quick_data_access.FTRepo;
import com.btw.tax_engine.quick_data_access.service.CacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.GZIPInputStream;

import static com.btw.tax_engine.common.Const.*;

public class CoreDataUpdateListener implements MessageListener {

    private static final Logger log = LoggerFactory.getLogger(CoreDataUpdateListener.class);

    protected YqRuleProvider yqRuleProvider;
    protected TaxRuleProvider taxRuleProvider;
    protected CacheService cs;
    protected FTRepo ftRepo;

    public CoreDataUpdateListener(YqRuleProvider yqRuleProvider, TaxRuleProvider taxRuleProvider,
                                  CacheService cs, FTRepo ftRepo) {
        this.yqRuleProvider = yqRuleProvider;
        this.taxRuleProvider = taxRuleProvider;
        this.cs = cs;
        this.ftRepo = ftRepo;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String msg = message.toString();
        log.info("[Core Data] Message received: {}", msg);
        switch (msg) {
            case MSG_S1_CT:
                yqRuleProvider.switchYQDataMap(true);
                evictMiddleResults();
                evictAllYQYRSubsidiary();
                break;
            case MSG_X1_CT:
                taxRuleProvider.switchTaxDataMap(true);
                evictMiddleResults();
                evictAllTTBSSubsidiary();
                break;
            case MSG_PFC:
                taxRuleProvider.switchXFDataMap();
                evictMiddleResults();
                break;
            case MSG_ICER:
                evictMiddleResults();
                evictAllIcers();
                break;
            default:
                process_inc_data(msg, true);
        }
    }

    protected void process_inc_data(String msg, boolean useRedisA) {
        if (msg.startsWith("incremental_data") || msg.startsWith("sudden_incremental_data")) {
            String[] tns = msg.split("\\|")[1].split(",");
            boolean hasX1S1 = Arrays.stream(tns).anyMatch(tn -> ("X1".equals(tn) || "S1".equals(tn)));
            if (hasX1S1) {
                process_inc_core_data(useRedisA, msg.startsWith("sudden_incremental_data"));
            }
            boolean hasS1 = false;
            for (String tn : tns) {
                if (!"X1".equals(tn) && !"S1".equals(tn)) {
                    String mn = "evictAll" + tn + "Cache";
                    try {
                        Method csInstanceMethod = CacheService.class.getMethod(mn);
                        csInstanceMethod.invoke(cs);
                    } catch (Exception e) {
                        log.warn(e.getMessage(), e);
                    }
                } else if ("S1".equals(tn)) {
                    hasS1 = true;
                }
            }
            if (tns.length > 0) {
                if (hasS1) {
                    cs.evictAllcheckLocationCache();
                }
                evictMiddleResults();
            }
        }
    }

    private void process_inc_core_data(boolean useRedisA, boolean suddenFlag) {
        String inc_core_filename = suddenFlag ? "sudden_incremental_x1s1.txt.gz" : "incremental_x1s1.txt.gz";
        ftRepo.download(inc_core_filename);
        String localPath = System.getProperty("java.io.tmpdir");
        Path p = Paths.get(localPath, inc_core_filename);
        String pstr = p.toString();
        log.info("Began to process {}", pstr);
        Map<String, List<YqYrRule>> delYListMap = new HashMap<>();
        List<String> updYList = new ArrayList<>();
        List<TaxRule> delTList = new ArrayList<>();
        List<String> updTList = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(p.toFile());
             GZIPInputStream gis = new GZIPInputStream(fis);
             InputStreamReader isr = new InputStreamReader(gis);
             BufferedReader br = new BufferedReader(isr)) {

            String line;
            YqYrRule y;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (StringUtils.hasLength(line)) {
                    if (line.startsWith("D|")) {
                        String tn = SU.nthsec(line, '|', 1);
                        String[] fs = SU.nthsec(line, '|', 2).split(",");
                        if ("X1".equals(tn)) {
                            delTList.add(new TaxRule(fs));
                        } else if ("S1".equals(tn)) {
                            y = new YqYrRule(fs);
                            if (!delYListMap.containsKey(y.cxr_code)){
                                delYListMap.put(y.cxr_code, new ArrayList<>());
                            }
                            delYListMap.get(y.cxr_code).add(y);
                        }
                    } else {
                        String tn = SU.nthsec(line, '|', 0);
                        String fstr = SU.nthsec(line, '|', 1);
                        if ("X1".equals(tn)) {
                            updTList.add(fstr);
                        } else if ("S1".equals(tn)) {
                            updYList.add(fstr);
                        }
                    }
                    log.debug(line);
                }
            }

            if (delYListMap.size()>0 || updYList.size()>0) {
                yqRuleProvider.process_inc_data(delYListMap, updYList, useRedisA);
            }
            if (delTList.size()>0 || updTList.size()>0) {
                taxRuleProvider.process_inc_data(delTList, updTList, useRedisA);
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        log.info("Complete processing {}", pstr);
    }

    protected void evictMiddleResults() {
        cs.evictAllItineraryCache();
        cs.evictAllItinerarySectorCache();
    }

    protected void evictAllTTBSSubsidiary() {
        cs.evictAllPT1Cache();
        cs.evictAllPT2Cache();
        cs.evictAllPT3Cache();
        cs.evictAllPJ1Cache();
        cs.evictAllPJ2Cache();
        cs.evictAllPJVCache();
        cs.evictAllPJWCache();

        cs.evictAllT167XCache();
        cs.evictAllT167YCache();
        cs.evictAllT168Cache();
        cs.evictAllT169Cache();
        cs.evictAllT183Cache();
        cs.evictAllT186Cache();
        cs.evictAllT190Cache();

        cs.evictAllC2SSCache();
        cs.evictAllC2NCache();
        cs.evictAllN2ACache();
        cs.evictAllSS2ACache();
        cs.evictAllS2ACache();
        cs.evictAllZ2ACache();

        cs.evictAllraCache();
    }
    protected void evictAllYQYRSubsidiary() {
        cs.evictAllcheckLocationCache();
        cs.evictAllPJLCache();
        cs.evictAllPSLCache();
        cs.evictAllPSVCache();
        cs.evictAllY198Cache();
    }

    protected void evictAllIcers() {
        cs.evictAllicerCache();
        cs.evictAllicerFlagCache();
    }

}