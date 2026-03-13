package com.example.srinference.core;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ModelRegistry {

    private final Map<ModelKey, SRModel> models = new ConcurrentHashMap<>();

    public void register(ModelKey key, SRModel model) {
        models.put(key, model);
    }

    public SRModel get(String name, Integer scale) {
        return models.get(new ModelKey(name, scale));
    }

    @PreDestroy
    public void shutdown() {
        log.debug("开始释放模型");
        models.values().forEach(model -> {
            try {
                model.close();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        });
        log.debug("释放模型完毕");
    }
}
