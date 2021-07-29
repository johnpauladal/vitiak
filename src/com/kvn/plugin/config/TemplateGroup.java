package com.kvn.plugin.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kvn.plugin.Debugger;
import com.kvn.plugin.presistentConfig.PersistentConfig;
import com.kvn.plugin.tools.FileUtils;
import com.kvn.plugin.ui.SelectConfig2Generate;
import lombok.Data;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

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

    public static void main(String[] args) throws Exception {
        String path = "file:\\C:/Users/809693/.IntelliJIdea2018.1/config/plugins/kvn-code-plugin/lib/kvn-code-plugin.jar!/com/kvn/plugin/config/";
        path = path.substring(0, path.lastIndexOf("!"));
        try (JarFile jarFile = new JarFile(path)){
            Enumeration<JarEntry> entries = jarFile.entries();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("args = [" + path + "]");
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

        if (Debugger.DEBUG_SWITCH) {
            debugProcess(templateGroup, groupName);
        } else {
            productionProcess(templateGroup, groupName);
        }

        return templateGroup;
    }

    /**
     * 简单调试阶段使用此方法。插件没有打成jar，需要通过文件路径来取初始模板
     * @param templateGroup
     * @param groupName
     */
    private static void debugProcess(TemplateGroup templateGroup, String groupName) {
        String path = TemplateGroup.class.getResource("/template").getPath();
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
    }

    /**
     * <pre>
     * 确认要打包时使用此方法。
     * 打包后插件会被打成jar包，所以初始模板要从jar里面取
     * <b>NOTE:由于打包后不好调试，故使用Debugger.debug()的方式来调试</b>
     * </pre>
     * @param templateGroup
     * @param groupName
     */
    private static void productionProcess(TemplateGroup templateGroup, String groupName) {
        String path = TemplateGroup.class.getResource("").getPath();
        path = path.substring(0, path.lastIndexOf("!"));
        if (path.startsWith("file:")) {
            path = path.substring(6);
        }
//        Debugger.debug("path0 = [" + TemplateGroup.class.getResource("").getPath() + "]" + "\r\n[" + TemplateGroup.class.getResource("/").getPath() + "]");
        String jarFileName = path;
        try (JarFile jarFile = new JarFile(jarFileName)) {
            // 遍历JAR文件
            Enumeration<JarEntry> entries = jarFile.entries();
//            Debugger.debug("1:" + entries.toString());
            String prefix = "template/" + groupName;
//            Debugger.debug("2:" + prefix);
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();
                // 目录跳过
                if (jarEntry.isDirectory()) {
                    continue;
                }
                String name = jarEntry.getName();
                if (name.startsWith(prefix)) {
                    String templatePath = "/" + name;
                    name = name.substring(name.lastIndexOf("/") + 1, name.length() - 3);
                    templateGroup.getElementList().add(new Template(name, loadTemplate(templatePath)));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Debugger.debug("3:" + e.getMessage());
        }
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


    private static void debug(Object obj){
        throw new RuntimeException(obj.toString());
    }



}
