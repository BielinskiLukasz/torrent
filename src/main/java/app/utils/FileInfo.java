package app.utils;

import app.config.Config;

public class FileInfo {

    int clientId;
    String name;
    String md5;

    FileInfo(String fileName, int clientId) {
        String filePath = Config.BASIC_PATH + clientId + "\\" + fileName;

        this.clientId = clientId;
        this.name = fileName;
        this.md5 = MD5Sum.md5(filePath);
    }

    FileInfo(String data) {
        String[] splittedData = data.split("\\|");

        this.clientId = Integer.parseInt(splittedData[0]);
        this.name = splittedData[1];
        this.md5 = splittedData[2];
    }
}
