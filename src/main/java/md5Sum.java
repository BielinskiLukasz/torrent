import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class md5Sum {

    static String md5(String input) throws NoSuchAlgorithmException {
        MessageDigest mDigest = MessageDigest.getInstance("MD5");
        byte[] result = mDigest.digest(input.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : result) {
            sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }

    public static void main(String[] args) {

    }

}
