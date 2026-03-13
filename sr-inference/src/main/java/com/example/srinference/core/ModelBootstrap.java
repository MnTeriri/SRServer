package com.example.srinference.core;

import com.example.srcommon.config.SRProperties;
import com.example.srinference.model.TorchScriptSRModel;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class ModelBootstrap {
    @Autowired
    private SRProperties properties;

    @Autowired
    private ModelRegistry modelRegistry;

    public ModelBootstrap() {
        log.debug("创建Component对象：{}", this);
    }

    @PostConstruct
    public void init() throws Exception {
        log.info("初始化超分模型");
        for (var modelEntry : properties.getModels().entrySet()) {
            String modelName = modelEntry.getKey();
            List<SRProperties.ModelConfig> modelConfigs = modelEntry.getValue();
            for (SRProperties.ModelConfig modelConfig : modelConfigs) {
                Integer scale = modelConfig.getScale();
                String modelPath = properties.getModelDir() + modelConfig.getPath();
                String device = modelConfig.getDevice();

                log.info("初始化模型：模型名称：{}，放大倍率：{}，权重路径：{}，运行设备：{}", modelName, scale, modelPath, device);

                SRModel model = new TorchScriptSRModel(modelName, scale, modelPath, device);
                modelRegistry.register(new ModelKey(modelName, scale), model);
            }
        }
    }
}
