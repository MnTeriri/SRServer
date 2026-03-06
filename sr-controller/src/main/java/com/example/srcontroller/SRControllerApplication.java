package com.example.srcontroller;

import com.example.srcommon.config.SRProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@EnableDiscoveryClient
@Import(SRProperties.class)
public class SRControllerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SRControllerApplication.class, args);
    }

}
