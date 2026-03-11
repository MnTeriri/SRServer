package com.example.srinference.model;

import ai.djl.inference.Predictor;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.NoopTranslator;
import com.example.srinference.core.SRModel;
import com.example.srinference.utils.ImageUtils;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Paths;

@Slf4j
@Getter
@ToString
public class TorchScriptSRModel implements SRModel {
    private final Criteria<NDList, NDList> criteria;

    private final String modelName;
    private final Integer scale;
    private final String path;


    public TorchScriptSRModel(String modelName, Integer scale, String path) {
        this.modelName = modelName;
        this.scale = scale;
        this.path = path;

        criteria = Criteria.builder()
                .setTypes(NDList.class, NDList.class)
                .optEngine("PyTorch")
                .optModelPath(Paths.get(path))
                .optTranslator(new NoopTranslator())
                .build();

    }

    @Override
    public void infer(String inputPath, String outputPath) throws Exception {
        try (ZooModel<NDList, NDList> model = criteria.loadModel();
             Predictor<NDList, NDList> predictor = model.newPredictor();
             NDManager manager = NDManager.newBaseManager()) {

            log.debug("正在处理图片 {}", inputPath);

            //1.图片转换成NDArray
            NDArray input = ImageUtils.toNDArray(inputPath, manager);
            log.info("Input shape: {}", input.getShape());

            //2.模型推理
            NDList outputs = predictor.predict(new NDList(input));
            NDArray output = outputs.singletonOrThrow();
            log.info("Output shape: {}", output.getShape());

            //3.NDArray转换成图片
            BufferedImage image = ImageUtils.toImage(output);
            ImageIO.write(image, outputPath.split("\\.")[1], new File(outputPath));
            log.debug("图片已保存在 {}", outputPath);
        }

    }
}
