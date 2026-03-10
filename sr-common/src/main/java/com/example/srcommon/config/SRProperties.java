package com.example.srcommon.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "sr")//自动刷新
public class SRProperties {
    private String inputDir;//照片上传路径
    private String outputDir;//超分照片保存路径

    private Map<String, List<ModelConfig>> models = new HashMap<>();

    @Data
    public static class ModelConfig {
        private Integer scale;
        private String path;
    }
}
