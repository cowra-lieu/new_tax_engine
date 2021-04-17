package com.btw.tax_engine.computing_core;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@ComponentScan(basePackages = {"com.btw.tax_engine.common", "com.btw.tax_engine.quick_data_access",
        "com.btw.tax_engine.computing_core"})
@EnableCaching
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class TestConfig {


}
