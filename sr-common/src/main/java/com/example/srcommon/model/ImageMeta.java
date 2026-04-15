package com.example.srcommon.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ImageMeta {
    private Integer width;
    private Integer height;
    private Long sizeBytes;

    public Double toKB() {
        return sizeBytes / 1024.0;
    }

    public Double toMB() {
        return sizeBytes / (1024.0 * 1024.0);
    }

    public String formatSize() {
        if (sizeBytes == null) return "0 B";

        if (sizeBytes < 1024) {
            return sizeBytes + " B";
        } else if (sizeBytes < 1024 * 1024) {
            return String.format("%.2f KB", toKB());
        } else {
            return String.format("%.2f MB", toMB());
        }
    }
}
