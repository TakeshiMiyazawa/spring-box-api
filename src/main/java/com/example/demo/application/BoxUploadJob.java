package com.example.demo.application;
import com.example.demo.service.BoxUploadService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.UUID;
import java.util.Date;


@Component
public class BoxUploadJob implements CommandLineRunner {

    private final BoxUploadService boxUploadService;
    private static final Logger logger = LoggerFactory.getLogger(BoxUploadJob.class);

    public BoxUploadJob(BoxUploadService boxUploadService) {
        this.boxUploadService = boxUploadService;
    }

    @Override
    public void run(String... args) {

        // Uncomment to test authentication to Box API
        // boxUploadService.listFilesInBox();
        

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String uuid = UUID.randomUUID().toString();
        String originalFilePath = "data/sample.csv";
        String uploadFileName = "sample_" + timestamp + "_" + uuid + ".csv";

        long start = System.currentTimeMillis();
        boxUploadService.uploadFileToBox(originalFilePath, uploadFileName);
        long end = System.currentTimeMillis();
        long duration = end - start;

        logger.info("ファイルアップロード所要時間: {} ms", duration);
    }
}