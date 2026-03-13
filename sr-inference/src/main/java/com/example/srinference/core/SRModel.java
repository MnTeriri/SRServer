package com.example.srinference.core;

public interface SRModel extends AutoCloseable {

    void infer(String inputPath, String outputPath) throws Exception;

}
