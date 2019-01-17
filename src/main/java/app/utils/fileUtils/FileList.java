package app.utils.fileUtils;

import app.config.Config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FileList {

    public static List<FileInfo> getFileInfoList(int clientNumber) {
        List<String> clientFileNameList = getFileList(Config.BASIC_PATH + clientNumber);

        List<FileInfo> clientFileInfoList = new ArrayList<>();
        clientFileNameList.forEach(
                file -> clientFileInfoList.add(new FileInfo(file, clientNumber))
        );

        return clientFileInfoList;
    }

    public static List<String> getFileNameList(int clientNumber) {
        return getFileList(Config.BASIC_PATH + clientNumber);
    }

    private static List<String> getFileList(String directoryPath) {
        List<String> directoryFileList = new ArrayList<>();
        File folder = new File(directoryPath);
        File[] files = folder.listFiles();

        for (File file : Objects.requireNonNull(files)) {
            if (file.isFile()) {
                directoryFileList.add(file.getName());
            }
        }

        return directoryFileList;
    }

    public static List<String> packFileInfoList(List<FileInfo> clientFileInfoList) {
        List<String> readyToSendList = new ArrayList<>();

        clientFileInfoList.forEach(
                fileInfo -> readyToSendList.add(fileInfo.clientId + "|" + fileInfo.name + "|" + fileInfo.md5)
        );

        return readyToSendList;
    }

    public static FileInfo unpackFileInfo(String packedFileInfo) {
        return new FileInfo(packedFileInfo);
    }
}
