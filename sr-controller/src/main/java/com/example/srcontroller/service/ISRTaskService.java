package com.example.srcontroller.service;

import com.example.srcommon.model.SRTask;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ISRTaskService {
    public String submit(MultipartFile uploadFile, String modelName, Integer scale) throws IOException;

    public List<Map<String, Object>> getModelList();

    public SRTask searchSRTaskByTaskId(String taskId);
}
