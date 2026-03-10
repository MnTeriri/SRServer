package com.example.srinference.core;

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

}
