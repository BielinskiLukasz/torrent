package app;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;

class FileInfo {

    int clientId;
    String name;
    String md5;

    FileInfo(String fileName, int clientId) {
        this.clientId = clientId;
        this.name = fileName;

        byte[] data = new byte[0];
        Path filePath = Paths.get(Config.BASIC_PATH + clientId + "\\" + fileName);

        try {
            data = Files.readAllBytes(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            this.md5 = MD5Sum.md5(data);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

    }

    FileInfo(String data) {
        String[] splitedData = data.split("\\|");

        this.clientId = Integer.parseInt(splitedData[0]);
        this.name = splitedData[1];
        this.md5 = splitedData[2];
    }
}
