package app.Utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class MD5Sum {

    static String md5(byte[] input) throws NoSuchAlgorithmException {
        MessageDigest mDigest = MessageDigest.getInstance("MD5");
        byte[] md5 = mDigest.digest(input);

        StringBuilder sb = new StringBuilder();
        for (byte b : md5) {
            sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }

}
