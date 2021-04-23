package wiki.blaze.util;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 文件类型过滤器
 * @Author wangcy
 * @Date 2021/4/22 17:20
 */
public class TypeFilter {

    public File sourceRoot;
    public File targetRoot;
    public Set<String> exts = new HashSet<>();

    public TypeFilter(String sourceRootPath, String targetRootPath, String ext) {
        this.sourceRoot = new File(sourceRootPath);
        this.targetRoot = new File(targetRootPath);
        this.exts.addAll(Arrays.stream(ext.split(",")).collect(Collectors.toSet()));
    }

    public boolean access(String filename) {
        if(StringUtils.isBlank(filename)) {
            return false;
        }
        int idx = filename.lastIndexOf(".");
        if(idx == -1) {
            return false;
        }
        return exts.contains(filename.substring(idx).toLowerCase());
    }

}
