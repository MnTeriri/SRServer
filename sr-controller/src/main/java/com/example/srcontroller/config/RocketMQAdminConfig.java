package com.example.srcontroller.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class RocketMQAdminConfig {

    public RocketMQAdminConfig() {
        log.debug("创建Configuration对象：{}", this);
    }

    @Value("${rocketmq.name-server}")
    private String namesrvAddr;

    @Bean
    public DefaultMQAdminExt mqAdminExt() throws MQClientException {
        DefaultMQAdminExt admin = new DefaultMQAdminExt();
        admin.setNamesrvAddr(namesrvAddr);
        admin.start();
        return admin;
    }
}