package app;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

class FileList {

    private static List<String> getFileList(String directoryPath) {
        List<String> directoryFileList = new ArrayList<>();
        File folder = new File(directoryPath);
        File[] listOfFiles = folder.listFiles();

        assert listOfFiles != null;
        for (File listOfFile : listOfFiles) {
            if (listOfFile.isFile()) {
                directoryFileList.add(listOfFile.getName());
            } /*else if (listOfFile.isDirectory()) {
                System.out.println("Directory " + listOfFile.getName());
            }*/ //Inner dirs are omitted
        }
        return directoryFileList;
    }

    static List<FileInfo> getFileInfoList(int clientNumber) {
        List<String> clientFileNameList = getFileList(Config.BASIC_PATH + clientNumber);

        List<FileInfo> clientFileInfoList = new ArrayList<>();
        clientFileNameList.forEach(
                file -> clientFileInfoList.add(new FileInfo(file, clientNumber))
        );

        return clientFileInfoList;
    }

    static List<String> packFileInfoList(List<FileInfo> clientFileInfoList) {
        List<String> readyToSendList = new ArrayList<>();

        clientFileInfoList.forEach(
                fileInfo -> readyToSendList.add(fileInfo.clientId + "|" + fileInfo.name + "|" + fileInfo.md5)
        );

        return readyToSendList;
    }

    static List<FileInfo> unpackFileInfoList(List<String> packedList) {
        List<FileInfo> unpackList = new ArrayList<>();

        packedList.forEach(
                data -> unpackList.add(new FileInfo(data))
        );

        return unpackList;
    }

    static FileInfo unpackFileInfo(String packedFileInfo) {
        return new FileInfo(packedFileInfo);
    }

}
