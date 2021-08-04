package com.kvn.plugin;

/**
 * Created by wangzhiyuan on 2018/8/6
 */
public class Debugger {
    /**
     * dubug开关。true：在idea环境下调试。false：打包插件时的设置
     */
    public static final boolean DEBUG_SWITCH = true;

    /**
     * 插件打包后，调试不方便。但是可以看到异常信息。所以取巧用异常来调试
     * @param obj
     */
    public static void debug(Object obj){
        throw new RuntimeException(obj.toString());
    }
}
