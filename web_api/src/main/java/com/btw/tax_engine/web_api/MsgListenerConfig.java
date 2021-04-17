package com.btw.tax_engine.web_api;

import com.btw.tax_engine.computing_core.tax.TaxRuleProvider;
import com.btw.tax_engine.computing_core.yqyr.YqRuleProvider;
import com.btw.tax_engine.quick_data_access.FTRepo;
import com.btw.tax_engine.quick_data_access.service.CacheService;
import com.btw.tax_engine.web_api.listener.CoreDataUpdateListener;
import com.btw.tax_engine.web_api.listener.CoreDataUpdateListenerB;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

import static com.btw.tax_engine.common.Const.TOPIC_CORE_UDPATE;

@Configuration
public class MsgListenerConfig {

    @Bean("topic4CoreData")
    ChannelTopic core_data_update_topic() {
        return new ChannelTopic(TOPIC_CORE_UDPATE);
    }

    @Bean("cduListener")
    MessageListener buildMessageListener(YqRuleProvider a, TaxRuleProvider b, CacheService c, FTRepo d) {
        return new CoreDataUpdateListener(a, b, c, d);
    }

    @Bean("cduListener_b")
    MessageListener buildMessageListener_b(YqRuleProvider a, TaxRuleProvider b, CacheService c, FTRepo d) {
        return new CoreDataUpdateListenerB(a, b, c, d);
    }

    @Bean
    RedisMessageListenerContainer redisContainer(@Qualifier("scf") RedisConnectionFactory redisConnFac,
                                                 @Qualifier("cduListener") MessageListener listener) {
        final RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory( redisConnFac );
        container.addMessageListener( new MessageListenerAdapter( listener ), core_data_update_topic() );
        return container;
    }

    @Bean
    RedisMessageListenerContainer redisContainer_b(@Qualifier("scf_b") RedisConnectionFactory redisConnFac,
                                                   @Qualifier("cduListener_b") MessageListener listener) {
        final RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory( redisConnFac );
        container.addMessageListener( new MessageListenerAdapter( listener ), core_data_update_topic() );
        return container;
    }

}
