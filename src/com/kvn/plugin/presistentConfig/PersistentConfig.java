package com.kvn.plugin.presistentConfig;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Transient;
import com.kvn.plugin.config.TemplateGroup;
import com.kvn.plugin.config.TemplateGroupEnum;
import lombok.Data;
import org.fest.util.Lists;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by wangzhiyuan on 2018/8/3
 */
@Data
@State(name = "KvnCodeSetting", storages = @Storage("kvn-code-setting.xml"))
public class PersistentConfig implements PersistentStateComponent<PersistentConfig> {

    @Transient
    private volatile boolean hasInitDefault;

    /**
     * 作者
     */
    private String author;

    /**
     * 当前配置的模板组名
     */
    private String currTemplateGroupName = TemplateGroupEnum.DEFAULT_GROUP_NAME.getPathName();

    /**
     * 用户设置的模板组
     */
    private Map<String, TemplateGroup> templateGroupMap;
    /**
     * 用户设置的全局配置
     */
    private List<PluginGlobalConfig> pluginGlobalConfigList;

    public TemplateGroup getCurrTemplateGroup(){
        return templateGroupMap.get(currTemplateGroupName);
    }

    /**
     * 获取单例实例对象
     * @return 实例对象
     */
    public static PersistentConfig instance() {
        // 此方法会调用 PersistentConfig#loadState()
        PersistentConfig persistentConfig = ServiceManager.getService(PersistentConfig.class);
        persistentConfig.initDefault();
        return persistentConfig;
    }


    /**
     * 初始化默认配置
     */
    private void initDefault() {
        if (hasInitDefault) {
            return;
        }
        synchronized (PersistentConfig.class) {
            if (hasInitDefault) {
                return;
            }
            doInit();
            hasInitDefault = true;
        }

    }

    public void doInit() {
        this.author = "Administrator";
        //配置默认模板
        if (this.templateGroupMap == null) {
            this.templateGroupMap = TemplateGroup.loadDefaultTemplateGroupMap();
        }
        if (pluginGlobalConfigList == null) {
            pluginGlobalConfigList = Lists.newArrayList(new PluginGlobalConfig("resultClass", "com.sf.avcp.core.common.base.Result"));
        }
    }

    @Nullable
    @Override
    public PersistentConfig getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull PersistentConfig persistentConfig) {
        // 备份初始配置
        Map<String, TemplateGroup> templateGroupMap = persistentConfig.getTemplateGroupMap();
        if (templateGroupMap == null) {
            persistentConfig.initDefault();
        }
        // 覆盖初始配置
        XmlSerializerUtil.copyBean(persistentConfig, this);

        // FIXME 已经合并不再重复合并
//        // 合并配置
//        templateGroupMap.forEach((name, templateGroup) -> {
//            if (this.getTemplateGroupMap().containsKey(name)) {
//                return;
//            }
//            this.getTemplateGroupMap().put(name, templateGroup);
//        });
    }
}
