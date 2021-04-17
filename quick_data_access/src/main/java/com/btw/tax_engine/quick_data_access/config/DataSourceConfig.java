package com.btw.tax_engine.quick_data_access.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource({"classpath:/config/ft.properties"})
public class DataSourceConfig {


}
