package com.kvn.plugin.config;

/**
 * Created by wangzhiyuan on 2018/8/6
 */
public enum TemplateGroupEnum {
    DEFAULT_GROUP_NAME("default-MyBatisPlus"),
    SF_GROUP_NAME("sf-MyBatisPlus");

    private String pathName;

    TemplateGroupEnum(String pathName) {
        this.pathName = pathName;
    }

    public String getPathName() {
        return pathName;
    }

    public static String[] groupArray(){
        TemplateGroupEnum[] values = TemplateGroupEnum.values();
        String[] groupArr = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            groupArr[i] = values[i].pathName;
        }
        return groupArr;
    }
}
