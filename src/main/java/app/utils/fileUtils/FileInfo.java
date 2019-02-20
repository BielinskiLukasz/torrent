package app.utils.fileUtils;

import app.config.Config;

public class FileInfo {

    final int clientId;
    final String name;
    final String md5;

    FileInfo(String fileName, int clientId) {
        String filePath = Config.BASIC_PATH + clientId + "\\" + fileName;

        this.clientId = clientId;
        this.name = fileName;
        this.md5 = MD5Sum.calculateMd5(filePath);
    }

    FileInfo(String data) {
        String[] splittedData = data.split("\\|");

        this.clientId = Integer.parseInt(splittedData[0]);
        this.name = splittedData[1];
        this.md5 = splittedData[2];
        if (splittedData.length > 3) {
            String filePath = Config.BASIC_PATH + clientId + "\\" + name;
        } else {
        }
    }

    public int getClientId() {
        return clientId;
    }

    public String getName() {
        return name;
    }

    public String getMd5() {
        return md5;
    }
}
