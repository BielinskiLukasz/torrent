import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GetFileList {

    private static List<String> clientFileList;

    private static List<String> getFileList(String directoryPath) {
        List<String> directoryFileList = new ArrayList<>();
        File folder = new File("D:\\TORrent_1");
        File[] listOfFiles = folder.listFiles();

        assert listOfFiles != null;
        for (File listOfFile : listOfFiles) {
            if (listOfFile.isFile()) {
                directoryFileList.add("File " + listOfFile.getName());
            } /*else if (listOfFile.isDirectory()) {
                System.out.println("Directory " + listOfFile.getName());
            }*/ //Inner dirs are omitted
        }
        return directoryFileList;
    }

    public static void main(String[] args) {
        clientFileList = getFileList("D:\\TORrent_1");
        clientFileList.forEach(System.out::println);
    }

}
