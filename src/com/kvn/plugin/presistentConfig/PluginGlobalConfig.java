package com.kvn.plugin.presistentConfig;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.fest.util.Lists;

import java.util.List;

/**
 * 全局配置，可以在模板中使用。会加入到vmContext中
 * Created by wangzhiyuan on 2018/8/6
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PluginGlobalConfig {
    /**
     * 名称
     */
    private String name;
    /**
     * 值
     */
    private String value;


    /**
     * 默认的全局配置
     * @return
     */
    public static List<PluginGlobalConfig> defaultPluginGlobalConfigList() {
        return Lists.newArrayList(new PluginGlobalConfig("resultClass", "com.sf.avcp.core.common.base.Result"),
                new PluginGlobalConfig("queryPageClass", "com.sf.avcp.core.common.base.QueryPage"));
    }
}
