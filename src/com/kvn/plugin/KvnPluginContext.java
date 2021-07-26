package com.kvn.plugin;

import com.intellij.database.psi.DbTable;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.kvn.plugin.config.Template;
import com.kvn.plugin.config.TemplateGroup;
import com.kvn.plugin.presistentConfig.PersistentConfig;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Plugin上下文
 */
@Getter
public class KvnPluginContext {
    private volatile static KvnPluginContext kvnPluginContext;

    /**
     * 单例模式
     */
    public static KvnPluginContext instance() {
        if (kvnPluginContext == null) {
            synchronized (KvnPluginContext.class) {
                if (kvnPluginContext == null) {
                    kvnPluginContext = new KvnPluginContext();
                }
            }
        }
        return kvnPluginContext;
    }

    private KvnPluginContext() {
    }



    /*************************************插件面板弹出后就确定的属性*************************************/
    /**
     * 模板组对象
     */
    private TemplateGroup selectedTemplateGroup;
    /**
     * 插件中配置好的模板组对象
     */
    private Map<String/*groupName*/, TemplateGroup> templateGroupMap;
    /**
     * 所有选中的表
     */
    private List<DbTable> dbTableList;
    /**
     * 项目中所有的modules
     */
    private Module[] modules;
    /**
     * 当前项目
     */
    private Project project;
    /**
     * 当前选中的表
     */
    private DbTable selectDbTable;

    /**
     * 获取用户当前选择的模板组
     * @return
     */
    public TemplateGroup getSelectedTemplateGroup(){
        return templateGroupMap.get(PersistentConfig.instance().getCurrTemplateGroupName());
    }


    /**
     * 当用户点击 File -> Settings 时，需要初始化的信息
     */
    public static void initAfterSettingsPop(){
        initTemplateGroupMap();
    }

    /**
     * 当弹出插件使用对话框后需要初始化的信息
     * @param templateGroup
     * @param dbTableList
     * @param modules
     * @param project
     * @param selectDbTable
     */
    public static void initAfterPluginPop(TemplateGroup templateGroup, List<DbTable> dbTableList, Module[] modules, Project project, DbTable selectDbTable) {
        KvnPluginContext instance = KvnPluginContext.instance();
        instance.selectedTemplateGroup = templateGroup;
        instance.dbTableList = dbTableList;
        instance.modules = modules;
        instance.project = project;
        instance.selectDbTable = selectDbTable;
        initTemplateGroupMap();
    }

    private static void initTemplateGroupMap() {
        KvnPluginContext instance = KvnPluginContext.instance();
        //配置默认模板
        if (instance.templateGroupMap == null) {
            instance.templateGroupMap = new LinkedHashMap<>();
        }
        instance.templateGroupMap = TemplateGroup.loadDefaultTemplateGroupMap();
    }


    /*****************************插件面板弹出后，生成代码文件前，由用户选择的属性*********************************/
    /**
     * 生成代码文件的保存根路径
     */
    private String savePath;
    /**
     * 选中的所有模板
     */
    private List<Template> selectTemplate;
    /**
     * 设置的包名称
     */
    private String packageName;
    /**
     * 选中的model
     */
    private Module selectModule;

    public static void initBeforeGenerate(String savePath, List<Template> selectTemplate, String packageName, Module selectModule) {
        KvnPluginContext instance = KvnPluginContext.instance();
        instance.savePath = savePath;
        instance.selectTemplate = selectTemplate;
        instance.packageName = packageName;
        instance.selectModule = selectModule;
    }
}
