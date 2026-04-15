package com.example.srcommon.utils;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.DataType;
import ai.djl.translate.Pipeline;
import com.example.srcommon.model.ImageMeta;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class ImageUtils {

    public static NDArray toNDArray(String imagePath, NDManager manager) throws Exception {
        BufferedImage bufferedImage = ImageIO.read(new File(imagePath));
        Image img = ImageFactory.getInstance().fromImage(bufferedImage);
        NDArray array = img.toNDArray(manager);

        Pipeline pipeline = new Pipeline().add(new ToTensor());   // HWC -> CHW 以及 [0-255] -> [0,1]
        array = pipeline.transform(new NDList(array)).singletonOrThrow();
        array = array.expandDims(0); // [1,3,H,W]

        return array;
    }


    public static BufferedImage toImage(NDArray ndArray) throws Exception {
        ndArray = ndArray.squeeze(); // [3,H,W]
        ndArray = ndArray.transpose(1, 2, 0); // HWC
        ndArray = ndArray.mul(255).clip(0, 255).toType(DataType.UINT8, true);

        return (BufferedImage) ImageFactory.getInstance()
                .fromNDArray(ndArray)
                .getWrappedImage();
    }

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