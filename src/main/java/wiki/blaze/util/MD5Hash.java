package wiki.blaze.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

/**
 * md5工具
 * @Author wangcy
 * @Date 2021/4/22 14:53
 */
public class MD5Hash {

    public static String digestAsHex(String content) {
        if(StringUtils.isBlank(content)) {
            throw new RuntimeException("content must not be null");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            return bytes2Hex(digest.digest(content.getBytes()));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static String digestAsHex(File file) {
        if(file == null || !file.exists()) {
            throw new RuntimeException("file must be exists");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            InputStream is = new FileInputStream(file);
            byte[] buf = new byte[1024];
            int len = -1;
            while((len = is.read(buf)) != -1) {
                digest.update(buf, 0, len);
            }
            return bytes2Hex(digest.digest());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static String bytes2Hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for(byte b : bytes) {
            sb.append(Integer.toHexString(b & 0xff));
        }
        return sb.toString();
    }

}
