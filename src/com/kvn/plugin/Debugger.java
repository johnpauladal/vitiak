package com.kvn.plugin;

/**
 * Created by wangzhiyuan on 2018/8/6
 */
public class Debugger {
    /**
     * dubug开关。true：在idea环境下调试。false：打包插件时的设置
     */
    public static final boolean DEBUG_SWITCH = false;
    /**
     * 插件版本，升级时使用。防止插件升级后，用户本地缓存的配置文件不兼容。
     */
    public static final String PLUGIN_VERSION = "V1.1";

    /**
     * 插件打包后，调试不方便。但是可以看到异常信息。所以取巧用异常来调试
     * @param obj
     */
    public static void debug(Object obj){
        throw new RuntimeException(obj.toString());
    }
}
