package com.example.srcontroller.service;

import com.example.srcommon.model.SRTask;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ISRTaskService {
    public String submit(MultipartFile uploadFile, String modelName, Integer scale) throws IOException;

    public SRTask searchSRTaskByTaskId(String taskId);
}
