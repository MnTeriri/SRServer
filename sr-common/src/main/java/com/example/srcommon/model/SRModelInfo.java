package com.example.srcommon.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SRModelInfo {
    private String modelName;
    private List<Integer> scales;
}
