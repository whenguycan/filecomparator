package wiki.blaze.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件模板，记录每个文件的md5
 * @Author wangcy
 * @Date 2021/4/22 14:26
 */
public class FileTemplate implements Serializable {
    private static final long serialVersionUID = 4444893949640543571L;

    Map<String, String> map = new HashMap<>();
    int changed;

    public int changed() {
        return this.changed;
    }

    public int size() {
        return map.size();
    }

    public void clearChanged() {
        changed = 0;
    }

    public void printf() {
        map.forEach((k, v) -> System.out.printf("md5: %s, file: %s%n", v, k));
    }

    public boolean changed(String path, String md5) {
        if(!map.containsKey(path)) {
            return true;
        }
        return !md5.equals(map.get(path));
    }

    public void update(String path, String md5) {
        map.put(path, md5);
        changed++;
    }

}
