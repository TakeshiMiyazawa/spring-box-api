package com.example.demo.service;

import com.box.sdk.BoxFile;
import com.example.demo.infrastructure.BoxClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;

@Service
public class BoxUploadService {

    private static final Logger logger = LoggerFactory.getLogger(BoxUploadService.class);
    private final BoxClient boxClient;

    public BoxUploadService(BoxClient boxClient) {
        this.boxClient = boxClient;
    }

    /**
     * ファイルをBoxにアップロード
     */
    public void uploadFile(Path filePath, String folderId) {
        String fileName = filePath.getFileName().toString();
        try (InputStream stream = new FileInputStream(filePath.toFile())) {
            BoxFile.Info fileInfo = boxClient.uploadFile(folderId, stream, fileName);
            logger.info("Uploaded: {} (ID: {})", fileInfo.getName(), fileInfo.getID());
        } catch (Exception e) {
            logger.error("Upload failed: {} - {}", fileName, e.getMessage());
            throw new RuntimeException("Failed to upload: " + fileName, e);
        }
    }
}