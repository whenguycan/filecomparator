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

    String PATCH_AND_TEMPLATE_OUTPUT_DIR = "C:\\_git_repo\\patches";
    String TEMPLATE_FILE_EXT = ".t";
    String PROJECT_NAME;
    String PROJECT_DIR;
    String PROJECT_CLASSES_DIR;
    String PATCH_CLASSES_DIR;
    String PROJECT_MAPPER_DIR;
    String PATCH_MAPPER_DIR;
    String PROJECT_WEB_INF_DIR;
    String PATCH_WEB_INF_DIR;

    List<TypeFilter> filters = new ArrayList<>();

    public FileComparator(String projectName) {
        PROJECT_NAME = projectName;
        if(PROJECT_NAME == null || PROJECT_NAME.length() == 0) {
            throw new RuntimeException("projectName is empty");
        }
        PROJECT_DIR = "C:\\_git_repo\\" + PROJECT_NAME;
        File dir = new File(PROJECT_DIR);
        if(!dir.exists()) {
            throw new RuntimeException("projectName is incorrect");
        }

        PROJECT_CLASSES_DIR = PROJECT_DIR + "\\target\\classes";
        PATCH_CLASSES_DIR = PATCH_AND_TEMPLATE_OUTPUT_DIR + "\\" + PROJECT_NAME + "\\WEB-INF\\classes";
        PROJECT_MAPPER_DIR = PROJECT_DIR + "\\src\\main\\resources\\mapper";
        PATCH_MAPPER_DIR = PATCH_AND_TEMPLATE_OUTPUT_DIR + "\\" + PROJECT_NAME + "\\WEB-INF\\classes\\mapper";
        PROJECT_WEB_INF_DIR = PROJECT_DIR + "\\src\\main\\webapp\\WEB-INF";
        PATCH_WEB_INF_DIR = PATCH_AND_TEMPLATE_OUTPUT_DIR + "\\" + PROJECT_NAME + "\\WEB-INF";

        filters.add(new TypeFilter(PROJECT_CLASSES_DIR, PATCH_CLASSES_DIR, ".class"));
        filters.add(new TypeFilter(PROJECT_MAPPER_DIR, PATCH_MAPPER_DIR, ".xml"));
        filters.add(new TypeFilter(PROJECT_WEB_INF_DIR, PATCH_WEB_INF_DIR, ".jsp,.jar"));
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

    public void patch(String baseTemplateName) {
        System.out.println("patch start");
        if(baseTemplateName == null || baseTemplateName.length() == 0) {
            throw new RuntimeException("templateName is empty");
        }
        File templateFile = new File(PATCH_AND_TEMPLATE_OUTPUT_DIR, baseTemplateName);
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