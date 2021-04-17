package com.btw.tax_engine.web_api.listener;

import com.btw.tax_engine.computing_core.tax.TaxRuleProvider;
import com.btw.tax_engine.computing_core.yqyr.YqRuleProvider;
import com.btw.tax_engine.quick_data_access.FTRepo;
import com.btw.tax_engine.quick_data_access.service.CacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;

import static com.btw.tax_engine.common.Const.MSG_S1_CT;
import static com.btw.tax_engine.common.Const.MSG_X1_CT;

public class CoreDataUpdateListenerB extends CoreDataUpdateListener {

    private static final Logger log = LoggerFactory.getLogger(CoreDataUpdateListenerB.class);

    public CoreDataUpdateListenerB(YqRuleProvider a, TaxRuleProvider b, CacheService c, FTRepo d) {
        super(a, b, c, d);
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String msg = message.toString();
        log.info("[Core Data] Message received: {}", msg);
        switch (msg) {
            case MSG_S1_CT:
                yqRuleProvider.switchYQDataMap(false);
                evictMiddleResults();
                evictAllYQYRSubsidiary();
                break;
            case MSG_X1_CT:
                taxRuleProvider.switchTaxDataMap(false);
                evictMiddleResults();
                evictAllTTBSSubsidiary();
                break;
            default:
                process_inc_data(msg, false);
        }
    }

}
