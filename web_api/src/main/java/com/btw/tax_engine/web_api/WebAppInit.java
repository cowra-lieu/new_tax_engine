package com.btw.tax_engine.web_api;

import com.btw.tax_engine.common.DEU;
import com.btw.tax_engine.quick_data_access.config.LettuceRedisConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import java.beans.Introspector;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Properties;

public class WebAppInit implements WebApplicationInitializer {

    private static final Logger log = LoggerFactory.getLogger(WebAppInit.class);

    @Override
    public void onStartup(ServletContext servletContext) {
        AnnotationConfigWebApplicationContext ctx = new AnnotationConfigWebApplicationContext();
        ctx.setBeanNameGenerator(new AnnotationBeanNameGenerator(){

            @Override
            public String generateBeanName(BeanDefinition definition, BeanDefinitionRegistry registry) {
                String beanClassName = Introspector.decapitalize(definition.getBeanClassName());
                if( beanClassName != null && beanClassName.startsWith("com.btw.tax_engine.") ) {
//                    log.warn(">>> 初始化Bean: " + beanClassName);
                    return beanClassName;
                }
                return super.generateBeanName(definition, registry);
            }

        });

        ctx.register(WebAppConfig.class);
        ctx.setServletContext(servletContext);
        ServletRegistration.Dynamic dynamic = servletContext.addServlet("dispatcher", new DispatcherServlet(ctx));
        dynamic.addMapping("/");
        dynamic.setLoadOnStartup(1);

        Properties props = new Properties();
        Properties props2 = new Properties();
        try {
            props.load(this.getClass().getResourceAsStream("/config/app.properties"));
            props2.load(this.getClass().getResourceAsStream("/config/redis.properties"));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }

        String key = "spring.profiles.active";
        String spa = props.getProperty(key);
        String selectReisUrl = props.getProperty("select.redis");
        String aHost = props2.getProperty("host");
        String aPort = props2.getProperty("port");
        log.info("url: {}, spa: {}, aHost: {}, aPort: {}", selectReisUrl, spa, aHost, aPort);
        selectRedis(selectReisUrl, spa, aHost, aPort);

        servletContext.setInitParameter(key, spa);

        DEU.DATA_REFRESH_INTERVAL = Long.parseLong(props.getProperty("dataRefreshInterval"));

        servletContext.setInitParameter("webAppRootKey", System.currentTimeMillis()+".root");

    }

    private void selectRedis(String url, String spa, String aHost, String aPort) {
        String redisA = aHost + ":" + aPort;
        try (ReadableByteChannel in = Channels.newChannel(new URL(url).openStream());
             ByteArrayOutputStream baos = new ByteArrayOutputStream();    // default space is 32 bytes
             WritableByteChannel out = Channels.newChannel(baos)) {

            ByteBuffer buffer = ByteBuffer.allocateDirect(8 * 1024);
            while (in.read(buffer) != -1 || buffer.position() > 0) {
                buffer.flip();
                out.write(buffer);
                buffer.compact();
            }

            String redis = baos.toString();
            log.info("select redis: {}", redis);
            LettuceRedisConfig.tUseA = LettuceRedisConfig.yUseA = (redisA.equals(redis));

        } catch (Exception e) {
            if ("dev".equals(spa)) {
                LettuceRedisConfig.tUseA = LettuceRedisConfig.yUseA = true;
            } else {
                log.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }
    }

}
