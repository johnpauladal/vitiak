package com.kvn.plugin.presistentConfig;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Transient;
import com.kvn.plugin.Debugger;
import com.kvn.plugin.config.TemplateGroup;
import com.kvn.plugin.config.TemplateGroupEnum;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
     * 版本号。升级的时候使用。防止缓存文件不兼容
     */
    private String version;

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

    public TemplateGroup getCurrTemplateGroup() {
        return templateGroupMap.get(currTemplateGroupName);
    }

    /**
     * 获取单例实例对象
     *
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
        this.version = Debugger.PLUGIN_VERSION;
        this.author = "Administrator";
        //配置默认模板
        if (this.templateGroupMap == null) {
            this.templateGroupMap = TemplateGroup.loadDefaultTemplateGroupMap();
        }
        if (pluginGlobalConfigList == null) {
            pluginGlobalConfigList = PluginGlobalConfig.defaultPluginGlobalConfigList();
        }
    }

    /**
     * 获取用户当前选择的模板组
     *
     * @return
     */
    public TemplateGroup getSelectedTemplateGroup() {
        return templateGroupMap.get(PersistentConfig.instance().getCurrTemplateGroupName());
    }

    @Nullable
    @Override
    public PersistentConfig getState() {
        return this;
    }

    /**
     * 加载缓存的配置
     *
     * @param cachedPersistentConfig 缓存的配置
     */
    @Override
    public void loadState(@NotNull PersistentConfig cachedPersistentConfig) {
        Map<String, TemplateGroup> cachedTemplateGroupMap = cachedPersistentConfig.getTemplateGroupMap();
        if (cachedTemplateGroupMap == null) {
            cachedPersistentConfig.initDefault();
        }
        // 用缓存的配置覆盖初始配置
        XmlSerializerUtil.copyBean(cachedPersistentConfig, this);

        // 插件版本升级后，持久化配置需要合并
        if (cachedPersistentConfig.getVersion() != null && cachedPersistentConfig.getVersion().equals(Debugger.PLUGIN_VERSION)) {
            return;
        }

        // 插件本身的 templateGroup 覆盖用户配置的 templateGroup
        Map<String, TemplateGroup> defaultTemplateGroupMap = TemplateGroup.loadDefaultTemplateGroupMap();
        cachedTemplateGroupMap.forEach((name, templateGroup) -> {
            if (!defaultTemplateGroupMap.containsKey(name)) {
                return;
            }
            this.getTemplateGroupMap().put(name, defaultTemplateGroupMap.get(name));
        });
    }
}
