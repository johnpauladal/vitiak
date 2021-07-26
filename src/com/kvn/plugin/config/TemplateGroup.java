package com.kvn.plugin.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kvn.plugin.presistentConfig.PersistentConfig;
import com.kvn.plugin.tools.FileUtils;
import com.kvn.plugin.ui.SelectConfig2Generate;
import lombok.Data;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * 模板分组类
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/07/18 09:33
 */
@Data
public class TemplateGroup implements AbstractGroup<Template> {
    /**
     * 分组名称
     */
    private String name;
    /**
     * 元素对象
     */
    private List<Template> elementList;

    /**
     * clone一份用户持久化的模板组。（从配置文件加载）
     * @return
     */
    public static Map<String, TemplateGroup> cloneTemplateGroupMapFromPersitentFile(){
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, TemplateGroup> templateGroupMap = new LinkedHashMap<>();
        PersistentConfig.instance().getTemplateGroupMap().forEach(new BiConsumer<String, TemplateGroup>() {
            @Override
            public void accept(String s, TemplateGroup templateGroup) {
                try {
                    TemplateGroup templateGroupClone = objectMapper.readValue(objectMapper.writeValueAsString(templateGroup), TemplateGroup.class);
                    templateGroupMap.put(s, templateGroupClone);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        return templateGroupMap.size() <= 0 ? loadDefaultTemplateGroupMap() : templateGroupMap;
    }

    /**
     * 加载插件默认的模板组
     * @return
     */
    public static Map<String, TemplateGroup> loadDefaultTemplateGroupMap(){
        //配置默认模板组
        Map<String, TemplateGroup> templateGroupMap = new LinkedHashMap<>();
        for (String groupName : TemplateGroupEnum.groupArray()) {
            templateGroupMap.put(groupName, TemplateGroup.loadTemplateGroup(groupName));
        }
        return templateGroupMap;
    }


    /**
     * 按照分组加载模板组
     *
     * @param groupName 组名
     * @return 模板组
     */
    public static TemplateGroup loadTemplateGroup(String groupName) {
        TemplateGroup templateGroup = new TemplateGroup();
        templateGroup.setName(groupName);
        templateGroup.setElementList(new ArrayList<>());

        String path = SelectConfig2Generate.class.getResource("/template").getPath();

        File[] files = new File(path).listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return groupName.equals(name);
            }
        });
        for (File file : files) {
            if (file.isFile()) {
                continue;
            }
            File[] tList = file.listFiles();
            for (File t : tList) {
                if (t.isDirectory()) {
                    continue;
                }
                String absolutePath = "/template/" + groupName + "/" + t.getName();
                templateGroup.getElementList().add(new Template(t.getName(), loadTemplate(absolutePath)));
            }
        }

        return templateGroup;
    }

    /**
     * 加载模板文件
     *
     * @param filePath 模板路径
     * @return 模板文件内容
     */
    private static String loadTemplate(String filePath) {
        return FileUtils.getInstance().read(SelectConfig2Generate.class.getResourceAsStream(filePath)).replaceAll("\r", "");
    }



}
