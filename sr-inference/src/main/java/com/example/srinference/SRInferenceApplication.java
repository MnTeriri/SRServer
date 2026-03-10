package com.example.srinference;

import com.example.srcommon.config.SRProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(SRProperties.class)
public class SRInferenceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SRInferenceApplication.class, args);
    }

}
