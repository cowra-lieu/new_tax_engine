package com.btw.tax_engine.web_api;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * A plain Spring Configuration class for Web App.
 *
 * @author Cowra Lieu
 * @since 0.1-SNAPSHOT
 */

@Configuration
@ComponentScan({"com.btw.tax_engine"})
@EnableWebMvc
@EnableAsync
@EnableCaching
@EnableAspectJAutoProxy
public class WebAppConfig implements WebMvcConfigurer {


}
