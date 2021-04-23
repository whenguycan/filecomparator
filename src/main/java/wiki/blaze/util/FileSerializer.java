package wiki.blaze.util;

import java.io.*;

/**
 * 序列化工具
 * @Author wangcy
 * @Date 2021/4/22 15:27
 */
public class FileSerializer {

    public static void serialize(Serializable o, File output) {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(output));
            oos.writeObject(o);
            oos.flush();
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static Object deserialize(File input) {
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(input));
            Object o = ois.readObject();
            ois.close();
            return o;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
