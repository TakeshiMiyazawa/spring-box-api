package com.example.demo.service;
import com.example.demo.infrastructure.BoxClient;
import java.io.File;
import org.springframework.stereotype.Service;

@Service
public class BoxUploadService {

    private final BoxClient boxClient;

    public BoxUploadService(BoxClient boxClient) {
        this.boxClient = boxClient;
    }

    public void uploadFileToBox(String filePath, String uploadFileName) {
        boxClient.upload(new File(filePath), uploadFileName);
    }

    public void listFilesInBox() {
        boxClient.listRootFolderItems();
    }
}
