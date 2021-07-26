package com.kvn.plugin.presistentConfig;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}
