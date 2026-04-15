package com.example.srcontroller.service;

import com.example.srcommon.model.SRModelInfo;
import com.example.srcommon.model.SRTask;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface ISRTaskService {
    public String submit(MultipartFile uploadFile, String modelName, Integer scale);

    public List<SRModelInfo> getModelList();

    public SRTask searchSRTaskByTaskId(String taskId);

    public List<SRTask> searchSRTaskList(Integer currentPage, Integer pageSize);

    public void deleteSRTaskByTaskId(String taskId);

    public Resource downloadTaskImage(String taskId);
}
