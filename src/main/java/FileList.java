import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileList {

    private static List<String> clientFileNameList;
    private static List<FileInfo> clientFileInfoList;

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

    public static void main(String[] args) {
        int clientId = 1;
        clientFileNameList = getFileList("D:\\TORrent_" + clientId);

        clientFileInfoList = new ArrayList<>();
        clientFileNameList.forEach(
                file -> clientFileInfoList.add(new FileInfo(file, clientId))
        );

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

}
