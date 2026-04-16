package com.example.srcommon.utils;

import com.example.srcommon.model.ImageMeta;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class ImageUtils {

    public static ImageMeta getImageMeta(String imagePath) throws IOException {
        // 文件大小
        File file = new File(imagePath);
        long sizeBytes = file.length();

        // 图片尺寸
        BufferedImage bufferedImage = ImageIO.read(file);
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();

        return new ImageMeta(width, height, sizeBytes);
    }

    public static ImageMeta getImageMeta(InputStream inputStream, Long sizeBytes) throws IOException {
        // 图片尺寸
        BufferedImage bufferedImage = ImageIO.read(inputStream);
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();

        return new ImageMeta(width, height, sizeBytes);
    }
}