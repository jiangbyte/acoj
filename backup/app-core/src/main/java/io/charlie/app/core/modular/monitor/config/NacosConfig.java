package io.charlie.app.core.modular.monitor.config;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.naming.NamingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class NacosConfig {

    @Value("${spring.cloud.nacos.server-addr}")
    private String nacosServerAddr;

    @Bean
    public NamingService namingServiceN() throws Exception {
        Properties properties = new Properties();
        properties.put("serverAddr", nacosServerAddr);
        return NacosFactory.createNamingService(properties);
    }
}