package com.example.demo.infrastructure;

import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxFile;
import com.box.sdk.BoxFolder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
public class BoxClient {

    @Value("${box.developer.token}")
    private String developerToken;

    private BoxAPIConnection api;

    /**
     * Box API接続を取得（遅延初期化）
     */
    private BoxAPIConnection getConnection() {
        if (api == null) {
            api = new BoxAPIConnection(developerToken);
        }
        return api;
    }

    /**
     * 指定フォルダにファイルをアップロード
     */
    public BoxFile.Info uploadFile(String folderId, InputStream inputStream, String fileName) {
        BoxFolder folder = new BoxFolder(getConnection(), folderId);
        return folder.uploadFile(inputStream, fileName);
    }

    /**
     * フォルダ情報を取得
     */
    public BoxFolder getFolder(String folderId) {
        return new BoxFolder(getConnection(), folderId);
    }
}