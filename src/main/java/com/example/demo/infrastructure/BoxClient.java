package com.example.demo.infrastructure;
import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxItem;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

@Component
public class BoxClient {

    private final BoxAPIConnection api;

    public BoxClient(@Value("${box.developer.token}") String developerToken) {
        this.api = new BoxAPIConnection(developerToken);
    }

    public void upload(File file, String uploadFileName) {
        BoxFolder rootFolder = BoxFolder.getRootFolder(api);
        try (FileInputStream fis = new FileInputStream(file)) {
            rootFolder.uploadFile(fis, uploadFileName);
            System.out.println("Uploaded file: " + uploadFileName);
        } catch (FileNotFoundException e) {
            System.err.println("ファイルが見つかりません: " + file.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("I/Oエラー: " + e.getMessage());
        }
    }

    public void listRootFolderItems() {
        BoxFolder rootFolder = BoxFolder.getRootFolder(api);
        for (BoxItem.Info itemInfo : rootFolder) {
            System.out.format("[%s] %s\n", itemInfo.getID(), itemInfo.getName());
        }
    }
}
