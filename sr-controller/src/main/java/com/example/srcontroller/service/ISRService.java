package com.example.srcontroller.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ISRService {
    public void submit(MultipartFile uploadFile, String modelName, Integer scale) throws IOException;
}
