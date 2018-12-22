import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Sum {

    static byte[] md5(byte[] input) throws NoSuchAlgorithmException {
        MessageDigest mDigest = MessageDigest.getInstance("MD5");
        return mDigest.digest(input);
    }

    public static void main(String[] args) {

    }

}
