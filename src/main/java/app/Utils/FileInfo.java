package app.Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;

public class FileInfo {

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
            System.out.println("FileInfo - read file bytes " + e);
            e.printStackTrace();
        }

        try {
            this.md5 = MD5Sum.md5(data);
        } catch (NoSuchAlgorithmException e) {
            System.out.println("FileInfo - calculate md5 sum " + e);
            e.printStackTrace();
        }
    }

    FileInfo(String data) {
        String[] splittedData = data.split("\\|");

        this.clientId = Integer.parseInt(splittedData[0]);
        this.name = splittedData[1];
        this.md5 = splittedData[2];
    }
}
