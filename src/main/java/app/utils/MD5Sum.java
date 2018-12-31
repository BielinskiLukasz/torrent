package app.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Sum {

    public static String md5(File file) {
        return md5(file.getPath());
    }

    public static String md5(String filePath) {
        byte[] data = readFileBytes(filePath);

        MessageDigest mDigest = null;
        try {
            mDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            ExceptionHandler.handle(e);
        }
        byte[] md5 = mDigest.digest(data);

        StringBuilder sb = new StringBuilder();
        for (byte b : md5) {
            sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }

    private static byte[] readFileBytes(String filePath) {
        byte[] data = new byte[0];
        try {
            data = Files.readAllBytes(Paths.get(filePath));
        } catch (IOException e) {
            ExceptionHandler.handle(e);
        }
        return data;
    }

    public static boolean check(String filePath, String md5Sum) {
        String checksum = md5(filePath);

        return checksum.equals(md5Sum);
    }
}
