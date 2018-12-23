package app;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileList {

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

    public static void main(String[] args) {
        List<FileInfo> clientFileInfoList = getFileInfoList(1);
        testGetFileInfoListResults(clientFileInfoList);
    }

    private static void testGetFileInfoListResults(List<FileInfo> clientFileInfoList) {
        clientFileInfoList.forEach(
                file -> {
                    System.out.println(file.clientId);
                    System.out.println(file.name);

                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < file.md5.length; i++) {
                        sb.append(Integer.toString((file.md5[i] & 0xff) + 0x100, 16).substring(1));
                    }
                    System.out.println(sb.toString());
                }
        );
    }

    static List<String> preparingFileInfoListToSend(List<FileInfo> clientFileInfoList) {
        List<String> readyToSendList = new ArrayList<>();

        clientFileInfoList.forEach(
                fileInfo -> {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < fileInfo.md5.length; i++) {
                        sb.append(Integer.toString((fileInfo.md5[i] & 0xff) + 0x100, 16).substring(1));
                    }

                    readyToSendList.add(fileInfo.clientId + ";" + fileInfo.name + ";" + sb.toString());
                }
        );

        return readyToSendList;
    }

}
