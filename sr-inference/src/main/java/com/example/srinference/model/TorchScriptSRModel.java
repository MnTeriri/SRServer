package com.example.srinference.model;

import ai.djl.Device;
import ai.djl.MalformedModelException;
import ai.djl.inference.Predictor;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.NoopTranslator;
import com.example.srcommon.config.SRProperties;
import com.example.srinference.core.SRModel;
import com.example.srinference.utils.ImageUtils;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Objects;

@Slf4j
@Getter
@ToString
public class TorchScriptSRModel implements SRModel {
    private final Criteria<NDList, NDList> criteria;
    private final ZooModel<NDList, NDList> model;
    private final Predictor<NDList, NDList> predictor;
    private final NDManager manager;


    private final String modelName;
    private final Integer scale;
    private final String path;
    private final Device device;


    public TorchScriptSRModel(String modelName, Integer scale, String path, String deviceName) throws Exception {
        this.modelName = modelName;
        this.scale = scale;
        this.path = path;
        this.device = Objects.equals(deviceName, "gpu") ? Device.gpu() : Device.cpu();

        this.criteria = Criteria.builder()
                .setTypes(NDList.class, NDList.class)
                .optEngine("PyTorch")
                .optDevice(this.device)
                .optModelPath(Paths.get(path))
                .optTranslator(new NoopTranslator())
                .build();

        this.model = criteria.loadModel();
        this.predictor = model.newPredictor();
        this.manager = NDManager.newBaseManager(device);

    }

    @Override
    public void infer(String inputPath, String outputPath) throws Exception {
        try (NDManager manager = this.manager.newSubManager()) {

            log.debug("正在处理图片 {}", inputPath);

            //1.图片转换成NDArray
            NDArray input = ImageUtils.toNDArray(inputPath, manager);
            log.info("Input shape: {}, Device: {}", input.getShape(), input.getDevice());

            //2.模型推理
            NDList outputs = predictor.predict(new NDList(input));
            NDArray output = outputs.singletonOrThrow();
            log.info("Output shape: {}, Device: {}", output.getShape(), output.getDevice());

            //3.NDArray转换成图片
            BufferedImage image = ImageUtils.toImage(output);
            ImageIO.write(image, outputPath.split("\\.")[1], new File(outputPath));
            log.debug("图片已保存在 {}", outputPath);
        }

    }

    @Override
    public void close() throws Exception {
        if (predictor != null) {
            predictor.close();
        }
        if (model != null) {
            model.close();
        }
        if (manager != null) {
            manager.close();
        }
    }
}
