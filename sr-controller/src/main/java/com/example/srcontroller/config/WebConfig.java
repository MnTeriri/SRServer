package com.example.srcontroller.config;

import com.example.srcommon.config.SRProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Slf4j
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private SRProperties srProperties;

    public WebConfig() {
        log.debug("创建Configuration对象：{}", this);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/image/inupts/**")
                .addResourceLocations("file:" + srProperties.getImageInputDir() + "/");

        registry.addResourceHandler("/image/outputs/**")
                .addResourceLocations("file:" + srProperties.getImageOutputDir() + "/");
    }
}
