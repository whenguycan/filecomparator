package wiki.blaze.comparator;

import wiki.blaze.util.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

/**
 * 文件对比工具
 * @Author wangcy
 * @Date 2021/4/22 13:53
 */
public class FileComparator {

    static final String PATCH_AND_TEMPLATE_OUTPUT_DIR = "C:\\_git_repo\\patches";
    static final String PROJECT_NAME = "3.3_Gemini_sldlxx";
    static final String PROJECT_DIR = "C:\\_git_repo\\3.3_Gemini_sldlxx";
    static final String TEMPLATE_FILE_EXT = ".t";
    static final String PROJECT_CLASSES_DIR = PROJECT_DIR + "\\target\\classes";
    static final String PATCH_CLASSES_DIR = PATCH_AND_TEMPLATE_OUTPUT_DIR + "\\" + PROJECT_NAME + "\\WEB-INF\\classes";
    static final String PROJECT_MAPPER_DIR = PROJECT_DIR + "\\src\\main\\resources\\mapper";
    static final String PATCH_MAPPER_DIR = PATCH_AND_TEMPLATE_OUTPUT_DIR + "\\" + PROJECT_NAME + "\\WEB-INF\\classes\\mapper";
    static final String PROJECT_WEB_INF_DIR = PROJECT_DIR + "\\src\\main\\webapp\\WEB-INF";
    static final String PATCH_WEB_INF_DIR = PATCH_AND_TEMPLATE_OUTPUT_DIR + "\\" + PROJECT_NAME + "\\WEB-INF";

    private FileComparator() {}

    List<TypeFilter> filters = new ArrayList<>();

    public static FileComparator getDefaultComparator() {
        FileComparator comparator = new FileComparator();
        comparator.filters.add(new TypeFilter(PROJECT_CLASSES_DIR, PATCH_CLASSES_DIR, ".class"));
        comparator.filters.add(new TypeFilter(PROJECT_MAPPER_DIR, PATCH_MAPPER_DIR, ".xml"));
        comparator.filters.add(new TypeFilter(PROJECT_WEB_INF_DIR, PATCH_WEB_INF_DIR, ".jsp"));
        return comparator;
    }

    public void createTemplate() {
        System.out.println("createTemplate start");
        FileTemplate template = new FileTemplate();
        this.filters.forEach(filter -> fillTemplate(template, filter, filter.sourceRoot));
        System.out.printf("%s file fill in template%n", template.size());
        File templateFile = new File(PATCH_AND_TEMPLATE_OUTPUT_DIR,
                StringUtils.concat(PROJECT_NAME, StringUtils.currentTimestamp(), TEMPLATE_FILE_EXT));
        FileSerializer.serialize(template, templateFile);
        System.out.printf("template file create at: %s%n", templateFile.getPath());
    }

    private void fillTemplate(FileTemplate template, TypeFilter filter, File file) {
        if(file != null && file.exists()) {
            if(file.isDirectory()) {
                Arrays.stream(file.listFiles()).forEach(f -> fillTemplate(template, filter, f));
            }else {
                if(filter.access(file.getName())) {
                    String path = file.getPath().replace(filter.sourceRoot.getPath(), "");
                    String md5 = MD5Hash.digestAsHex(file);
                    template.update(path, md5);
                    System.out.printf("file access in template: %s%n", path);
                }
            }
        }else {
            throw new RuntimeException(String.format("file not: %s", file.getPath()));
        }
    }

    public void patch(String templateName) {
        System.out.println("patch start");
        File templateFile = new File(PATCH_AND_TEMPLATE_OUTPUT_DIR, templateName);
        FileTemplate template = (FileTemplate) FileSerializer.deserialize(templateFile);
        template.clearChanged();
        String timestamp = StringUtils.currentTimestamp();
        this.filters.forEach(filter -> matchAndCopy(template, filter, filter.sourceRoot, timestamp));
        System.out.printf("%s file changed and copied%n", template.changed());
        File templateFileNew = new File(PATCH_AND_TEMPLATE_OUTPUT_DIR,
                StringUtils.concat(PROJECT_NAME, timestamp, TEMPLATE_FILE_EXT));
        FileSerializer.serialize(template, templateFileNew);
        System.out.printf("new template file create at: %s%n", templateFileNew.getPath());
    }

    private void matchAndCopy(FileTemplate template, TypeFilter filter, File file, String timestamp) {
        if(file != null && file.exists()) {
            if(file.isDirectory()) {
                Arrays.stream(file.listFiles()).forEach(f -> matchAndCopy(template, filter, f, timestamp));
            }else {
                if(filter.access(file.getName())) {
                    String path = file.getPath().replace(filter.sourceRoot.getPath(), "");
                    String md5 = MD5Hash.digestAsHex(file);
                    if(template.changed(path, md5)) {
                        template.update(path, md5);
                        File target = new File(
                                filter.targetRoot.getPath().replace(PROJECT_NAME, PROJECT_NAME + timestamp), path);
                        fileCopy(file, target);
                        System.out.printf("file changed and copy from : %s%n", file.getPath());
                        System.out.printf("-> to: %s%n", target.getPath());
                    }
                }
            }
        }else {
            throw new RuntimeException(String.format("file not: %s", file.getPath()));
        }
    }

    private void fileCopy(File source, File target) {
        try {
            if(!target.getParentFile().exists()) {
                target.getParentFile().mkdirs();
            }
            Files.copy(source.toPath(), new FileOutputStream(target));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}