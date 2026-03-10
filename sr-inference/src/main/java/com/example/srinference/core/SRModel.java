package com.example.srinference.core;

public interface SRModel {

    /**
     * 输入输出统一 Path，避免 NDArray 泄漏到上层
     */
    void infer(String inputPath, String outputPath) throws Exception;
}
