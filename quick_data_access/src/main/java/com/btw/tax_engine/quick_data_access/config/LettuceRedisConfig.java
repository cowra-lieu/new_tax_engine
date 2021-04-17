package com.btw.tax_engine.quick_data_access.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
@PropertySource("classpath:/config/redis.properties")
public class LettuceRedisConfig {

    public static volatile boolean yUseA = true;
    public static volatile boolean tUseA = true;

    @Value("${host}")
    private String host;
    @Value("${password}")
    private String password;
    @Value("${port}")
    private int port;

    @Value("${host.b}")
    private String host_b;
    @Value("${password.b}")
    private String password_b;
    @Value("${port.b}")
    private int port_b;

//    @Value("${timeout}")
//    private long timeout;
    @Value("${maxIdle}")
    private int maxIdle;
    @Value("${minIdle}")
    private int minIdle;
    @Value("${maxActive}")
    private int maxActive;
    @Value("${maxWait}")
    private long maxWait;
    @Value("${database}")
    private int database;

//    @Value("${cluster.nodes}")
//    private String clusterNodes;
//    @Value("${cluster.timeout}")
//    private int clusterTimeout;
//    @Value("${cluster.maxRedirects}")
//    private int clusterMaxRedirects;

    @Bean
    public JedisClientConfiguration jedisPoolConfig() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        //最大连接数
        jedisPoolConfig.setMaxTotal(maxActive);
        //最小空闲连接数
        jedisPoolConfig.setMinIdle(minIdle);
        //最大空闲连接数
        jedisPoolConfig.setMaxIdle(maxIdle);
        //当池内没有可用的连接时，最大等待时间
        jedisPoolConfig.setMaxWaitMillis(maxWait);
        //------其他属性根据需要自行添加-------------
        JedisClientConfiguration.JedisPoolingClientConfigurationBuilder jpcb =
                (JedisClientConfiguration.JedisPoolingClientConfigurationBuilder) JedisClientConfiguration.builder();
        jpcb.poolConfig(jedisPoolConfig);
        return jpcb.build();
    }

    @Bean("scf")
    public RedisConnectionFactory scf(JedisClientConfiguration jedisPoolConfig) {
        RedisStandaloneConfiguration jedisConfig = new RedisStandaloneConfiguration();
        jedisConfig.setDatabase(database);
        jedisConfig.setHostName(host);
        jedisConfig.setPort(port);
        jedisConfig.setPassword(RedisPassword.of(password));
        return new JedisConnectionFactory(jedisConfig, jedisPoolConfig);
    }

    @Bean("scf_b")
    public RedisConnectionFactory scf_b(JedisClientConfiguration jedisPoolConfig) {
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setDatabase(database);
        redisStandaloneConfiguration.setHostName(host_b);
        redisStandaloneConfiguration.setPort(port_b);
        redisStandaloneConfiguration.setPassword(RedisPassword.of(password_b));
        return new JedisConnectionFactory(redisStandaloneConfiguration, jedisPoolConfig);
    }

    @Bean("redisTemplate")
    public RedisTemplate<String, String> redisTemplate(@Qualifier("scf") RedisConnectionFactory connFactory) {
        return buildRedisTemplate(connFactory);
    }

    @Bean("redisTemplate_b")
    public RedisTemplate<String, String> redisTemplate_b(@Qualifier("scf_b") RedisConnectionFactory connFactory) {
        return buildRedisTemplate(connFactory);
    }



    private RedisTemplate<String, String> buildRedisTemplate(RedisConnectionFactory connFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        RedisSerializer<String> stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer );
        template.setValueSerializer(stringSerializer );
        template.setHashKeySerializer(stringSerializer );
        template.setHashValueSerializer(stringSerializer );
        template.setConnectionFactory(connFactory);
        template.afterPropertiesSet();
        return template;
    }

}
