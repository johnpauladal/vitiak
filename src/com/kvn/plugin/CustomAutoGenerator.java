/**
 * Copyright (c) 2011-2020, hubin (jobob@qq.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.kvn.plugin;

import com.baomidou.mybatisplus.activerecord.Model;
import com.baomidou.mybatisplus.annotations.TableLogic;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.annotations.Version;
import com.baomidou.mybatisplus.generator.InjectionConfig;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.po.TableField;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.baomidou.mybatisplus.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.toolkit.StringUtils;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.intellij.database.psi.DbTable;
import com.kvn.plugin.config.CustomConfigBuilder;
import com.kvn.plugin.config.Template;
import com.kvn.plugin.config.TemplateGroup;
import com.kvn.plugin.presistentConfig.PersistentConfig;
import com.kvn.plugin.tools.FileUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 生成文件
 *
 * @author YangHu, tangguo
 * @since 2016-08-30
 */
public class CustomAutoGenerator {

    private static final Logger logger = LoggerFactory.getLogger(CustomAutoGenerator.class);

    protected CustomConfigBuilder config;
    protected InjectionConfig injectionConfig;

    /**
     * 数据库表配置
     */
    private StrategyConfig strategy;
    /**
     * 包 相关配置
     */
    private PackageConfig packageInfo;
    /**
     * 模板 相关配置
     */
    private TemplateConfig template;

    /**
     * 全局 相关配置
     */
    private GlobalConfig globalConfig;
    /**
     * velocity引擎
     */
    private VelocityEngine engine;

    /**
     * 从Database插件面板上获取的表信息
     */
    private Collection<DbTable> dbTables;

    /**
     * 生成代码
     */
    public void execute() {
        logger.debug("==========================准备生成文件...==========================");
        // 初始化配置
        initConfig();
        // 创建输出文件路径
        mkdirs(config.getPathInfo());
        // 获取上下文
        Map<String, VelocityContext> ctxData = analyzeData(config);
        // 循环生成文件
        for (Map.Entry<String, VelocityContext> ctx : ctxData.entrySet()) {
            batchOutput(ctx.getKey(), ctx.getValue());
        }

        openOutputPath();
        logger.debug("==========================文件生成完成！！！==========================");
    }

    /**
     * 打开文件输出目录
     */
    private void openOutputPath() {
        if (config.getGlobalConfig().isOpen()) {
            try {
                String osName = System.getProperty("os.name");
                if (osName != null) {
                    if (osName.contains("Mac")) {
                        Runtime.getRuntime().exec("open " + config.getGlobalConfig().getOutputDir());
                    } else if (osName.contains("Windows")) {
                        Runtime.getRuntime().exec("cmd /c start " + config.getGlobalConfig().getOutputDir());
                    } else {
                        logger.debug("文件输出目录:" + config.getGlobalConfig().getOutputDir());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * <p>
     * 开放表信息、预留子类重写
     * </p>
     *
     * @param config 配置信息
     * @return
     */
    protected List<TableInfo> getAllTableInfoList(CustomConfigBuilder config) {
        return config.getTableInfoList();
    }

    /**
     * <p>
     * 分析数据
     * </p>
     *
     * @param config 总配置信息
     * @return 解析数据结果集
     */
    private Map<String, VelocityContext> analyzeData(CustomConfigBuilder config) {
        List<TableInfo> tableList = this.getAllTableInfoList(config);
        Map<String, String> packageInfo = config.getPackageInfo();
        Map<String, VelocityContext> ctxData = new HashMap<>();
        String superEntityClass = getSuperClassName(config.getSuperEntityClass());
        String superMapperClass = getSuperClassName(config.getSuperMapperClass());
        String superServiceClass = getSuperClassName(config.getSuperServiceClass());
        String superServiceImplClass = getSuperClassName(config.getSuperServiceImplClass());
        String superControllerClass = getSuperClassName(config.getSuperControllerClass());
        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        for (TableInfo tableInfo : tableList) {
            final VelocityContext ctx = new VelocityContext();
            if (null != injectionConfig) {
                /**
                 * 注入自定义配置
                 */
                injectionConfig.initMap();
                ctx.put("cfg", injectionConfig.getMap());
            }
            /* ---------- 添加导入包 ---------- */
            if (config.getGlobalConfig().isActiveRecord()) {
                // 开启 ActiveRecord 模式
                tableInfo.setImportPackages(Model.class.getCanonicalName());
            }
            if (tableInfo.isConvert()) {
                // 表注解
                tableInfo.setImportPackages(TableName.class.getCanonicalName());
            }
            if (tableInfo.isLogicDelete(config.getStrategyConfig().getLogicDeleteFieldName())) {
                // 逻辑删除注解
                tableInfo.setImportPackages(TableLogic.class.getCanonicalName());
            }
            if (StringUtils.isNotEmpty(config.getStrategyConfig().getVersionFieldName())) {
                // 乐观锁注解
                tableInfo.setImportPackages(Version.class.getCanonicalName());
            }
            if (StringUtils.isNotEmpty(config.getSuperEntityClass())) {
                // 父实体
                tableInfo.setImportPackages(config.getSuperEntityClass());
            } else {
                tableInfo.setImportPackages(Serializable.class.getCanonicalName());
            }
            // Boolean类型is前缀处理
            if (config.getStrategyConfig().isEntityBooleanColumnRemoveIsPrefix()) {
                for (TableField field : tableInfo.getFields()) {
                    if (field.getPropertyType().equalsIgnoreCase("boolean")) {
                        if (field.getPropertyName().startsWith("is")) {
                            field.setPropertyName(config.getStrategyConfig(),
                                    StringUtils.removePrefixAfterPrefixToLower(field.getPropertyName(), 2));
                        }
                    }
                }
            }
            // RequestMapping 连字符风格 user-info
            if (config.getStrategyConfig().isControllerMappingHyphenStyle()) {
                ctx.put("controllerMappingHyphenStyle", config.getStrategyConfig().isControllerMappingHyphenStyle());
                ctx.put("controllerMappingHyphen", StringUtils.camelToHyphen(tableInfo.getEntityPath()));
            }

            ctx.put("restControllerStyle", config.getStrategyConfig().isRestControllerStyle());
            ctx.put("package", packageInfo);
            GlobalConfig globalConfig = config.getGlobalConfig();
            ctx.put("author", globalConfig.getAuthor());
            ctx.put("idType", globalConfig.getIdType() == null ? null : globalConfig.getIdType().toString());
            ctx.put("logicDeleteFieldName", config.getStrategyConfig().getLogicDeleteFieldName());
            ctx.put("versionFieldName", config.getStrategyConfig().getVersionFieldName());
            ctx.put("activeRecord", globalConfig.isActiveRecord());
            ctx.put("kotlin", globalConfig.isKotlin());
            ctx.put("date", date);
            ctx.put("table", tableInfo);
            ctx.put("enableCache", globalConfig.isEnableCache());
            ctx.put("baseResultMap", globalConfig.isBaseResultMap());
            ctx.put("baseColumnList", globalConfig.isBaseColumnList());
            ctx.put("entity", tableInfo.getEntityName());
            ctx.put("entityColumnConstant", config.getStrategyConfig().isEntityColumnConstant());
            ctx.put("entityBuilderModel", config.getStrategyConfig().isEntityBuilderModel());
            ctx.put("entityLombokModel", config.getStrategyConfig().isEntityLombokModel());
            ctx.put("entityBooleanColumnRemoveIsPrefix", config.getStrategyConfig().isEntityBooleanColumnRemoveIsPrefix());
            ctx.put("superEntityClass", superEntityClass);
            ctx.put("superMapperClassPackage", config.getSuperMapperClass());
            ctx.put("superMapperClass", superMapperClass);
            ctx.put("superServiceClassPackage", config.getSuperServiceClass());
            ctx.put("superServiceClass", superServiceClass);
            ctx.put("superServiceImplClassPackage", config.getSuperServiceImplClass());
            ctx.put("superServiceImplClass", superServiceImplClass);
            ctx.put("superControllerClassPackage", config.getSuperControllerClass());
            ctx.put("superControllerClass", superControllerClass);

            /*********自定义扩展：添加全局配置************/
            PersistentConfig.instance().getPluginGlobalConfigList().forEach(data -> {
                ctx.put(data.getName(), data.getValue());
            });


            ctxData.put(tableInfo.getEntityName(), ctx);
        }



        return ctxData;
    }

    /**
     * <p>
     * 获取类名
     * </p>
     *
     * @param classPath
     * @return
     */
    private String getSuperClassName(String classPath) {
        if (StringUtils.isEmpty(classPath)) {
            return null;
        }
        return classPath.substring(classPath.lastIndexOf(".") + 1);
    }

    /**
     * <p>
     * 处理输出目录
     * </p>
     *
     * @param pathInfo 路径信息
     */
    private void mkdirs(Map<String, String> pathInfo) {
        for (Map.Entry<String, String> entry : pathInfo.entrySet()) {
            File dir = new File(entry.getValue());
            if (!dir.exists()) {
                boolean result = dir.mkdirs();
                if (result) {
                    logger.debug("创建目录： [" + entry.getValue() + "]");
                }
            }
        }
    }

    /**
     * <p>
     * 合成上下文与模板
     * </p>
     *
     * @param context vm上下文
     */
    private void batchOutput(String entityName, VelocityContext context) {
        try {
            TableInfo tableInfo = (TableInfo) context.get("table");
            Map<String, String> pathInfo = config.getPathInfo();
            String entityFile = String.format((pathInfo.get(ConstVal.ENTITY_PATH) + File.separator + "%s" + this.suffixJavaOrKt()), entityName);
            String mapperFile = String.format((pathInfo.get(ConstVal.MAPPER_PATH) + File.separator + tableInfo.getMapperName() + this.suffixJavaOrKt()), entityName);
            String xmlFile = String.format((pathInfo.get(ConstVal.XML_PATH) + File.separator + tableInfo.getXmlName() + ConstVal.XML_SUFFIX), entityName);
            String serviceFile = String.format((pathInfo.get(ConstVal.SERIVCE_PATH) + File.separator + tableInfo.getServiceName() + this.suffixJavaOrKt()), entityName);
            String implFile = String.format((pathInfo.get(ConstVal.SERVICEIMPL_PATH) + File.separator + tableInfo.getServiceImplName() + this.suffixJavaOrKt()), entityName);
            String controllerFile = String.format((pathInfo.get(ConstVal.CONTROLLER_PATH) + File.separator + tableInfo.getControllerName() + this.suffixJavaOrKt()), entityName);

            TemplateConfig template = config.getTemplate();

            TemplateGroup templateGroup = PersistentConfig.instance().getTemplateGroupMap().get(PersistentConfig.instance().getCurrTemplateGroupName());
            ImmutableMap<String, com.kvn.plugin.config.Template> templateImmutableMap = Maps.uniqueIndex(templateGroup.getElementList(), new Function<com.kvn.plugin.config.Template, String>() {
                @Nullable
                @Override
                public String apply(@Nullable com.kvn.plugin.config.Template template) {
                    return template.getName();
                }
            });

//            Debugger.debug(template.getController());

            // 根据override标识来判断是否需要创建文件
            if (template.getEntity(false) != null && isCreate(entityFile)) {
                vmToFile(context, template.getEntity(config.getGlobalConfig().isKotlin()), entityFile, templateImmutableMap);
            }
            if (template.getMapper() != null && isCreate(mapperFile)) {
                vmToFile(context, template.getMapper(), mapperFile, templateImmutableMap);
            }
            if (template.getXml() != null && isCreate(xmlFile)) {
                vmToFile(context, template.getXml(), xmlFile, templateImmutableMap);
            }
            if (template.getService() != null && isCreate(serviceFile)) {
                vmToFile(context, template.getService(), serviceFile, templateImmutableMap);
            }
            if (template.getServiceImpl() != null && isCreate(implFile)) {
                vmToFile(context, template.getServiceImpl(), implFile, templateImmutableMap);
            }
            if (template.getController() != null && isCreate(controllerFile)) {
                vmToFile(context, template.getController(), controllerFile, templateImmutableMap);
            }
            if (injectionConfig != null) {
                /**
                 * 输出自定义文件内容
                 */
                List<FileOutConfig> focList = injectionConfig.getFileOutConfigList();
                if (CollectionUtils.isNotEmpty(focList)) {
                    for (FileOutConfig foc : focList) {
                        // 判断自定义文件是否存在
                        if (isCreate(foc.outputFile(tableInfo))) {
                            vmToFile(context, foc.getTemplatePath(), foc.outputFile(tableInfo), templateImmutableMap);
                        }
                    }
                }
            }

        } catch (IOException e) {
            logger.error("无法创建文件，请检查配置信息！", e);
        }
    }

    /**
     * 文件后缀
     */
    protected String suffixJavaOrKt() {
        return config.getGlobalConfig().isKotlin() ? ConstVal.KT_SUFFIX : ConstVal.JAVA_SUFFIX;
    }

    /**
     * <p>
     * 将模板转化成为文件
     * </p>
     *  @param context      内容对象
     * @param templatePath 模板文件
     * @param outputFile   文件生成的目录
     * @param templateImmutableMap
     */
    private void vmToFile(VelocityContext context, String templatePath, String outputFile, ImmutableMap<String, Template> templateImmutableMap) throws IOException {
        if (StringUtils.isEmpty(templatePath)) {
            return;
        }

        String key = templatePath.substring(templatePath.lastIndexOf("/") + 1);
        com.kvn.plugin.config.Template template = templateImmutableMap.get(key);
//        Debugger.debug(templatePath.substring(templatePath.lastIndexOf("/") + 1) + "====>" + templateImmutableMap);
        if (template == null) {
            key = key.substring(0, key.length() - 3);
            template = templateImmutableMap.get(key);
        }
        VelocityEngine velocityEngine = new VelocityEngine();
        StringWriter stringWriter = new StringWriter();
        velocityEngine.setProperty("input.encode", "UTF-8");
        velocityEngine.setProperty("output.encode", "UTF-8");
        velocityEngine.evaluate(context, stringWriter, "Velocity Code Generate", template.getCode());
        String content = stringWriter.toString();


        File file = new File(outputFile);
        if (!file.getParentFile().exists()) {
            // 如果文件所在的目录不存在，则创建目录
            if (!file.getParentFile().mkdirs()) {
                logger.debug("创建文件所在的目录失败!");
                return;
            }
        }

        FileUtils.getInstance().write(file, content);

        logger.debug("模板:" + templatePath + ";  文件:" + outputFile);
    }

    /**
     * 设置模版引擎，主要指向获取模版路径
     */
    private VelocityEngine getVelocityEngine() {
        if (engine == null) {
            Properties p = new Properties();
            p.setProperty(ConstVal.VM_LOADPATH_KEY, ConstVal.VM_LOADPATH_VALUE);
//            p.setProperty(Velocity.FILE_RESOURCE_LOADER_PATH, CustomAutoGenerator.class.getResource("/").getPath());
            p.setProperty(Velocity.ENCODING_DEFAULT, ConstVal.UTF8);
            p.setProperty(Velocity.INPUT_ENCODING, ConstVal.UTF8);
            p.setProperty("file.resource.loader.unicode", "true");
            engine = new VelocityEngine(p);
        }
        return engine;
    }

    /**
     * 检测文件是否存在
     *
     * @return 是否
     */
    private boolean isCreate(String filePath) {
        File file = new File(filePath);
        return !file.exists() || config.getGlobalConfig().isFileOverride();
    }

    // ==================================  相关配置  ==================================

    /**
     * 初始化配置
     */
    protected void initConfig() {
        if (null == config) {
            config = new CustomConfigBuilder(packageInfo, strategy, template, globalConfig, dbTables);
//            if (null != injectionConfig) {
//                injectionConfig.setConfig(config);
//            }
        }
    }


    public StrategyConfig getStrategy() {
        return strategy;
    }

    public CustomAutoGenerator setStrategy(StrategyConfig strategy) {
        this.strategy = strategy;
        return this;
    }

    public PackageConfig getPackageInfo() {
        return packageInfo;
    }

    public CustomAutoGenerator setPackageInfo(PackageConfig packageInfo) {
        this.packageInfo = packageInfo;
        return this;
    }

    public TemplateConfig getTemplate() {
        return template;
    }

    public CustomAutoGenerator setTemplate(TemplateConfig template) {
        this.template = template;
        return this;
    }

    public CustomConfigBuilder getConfig() {
        return config;
    }

    public CustomAutoGenerator setConfig(CustomConfigBuilder config) {
        this.config = config;
        return this;
    }

    public GlobalConfig getGlobalConfig() {
        return globalConfig;
    }

    public CustomAutoGenerator setGlobalConfig(GlobalConfig globalConfig) {
        this.globalConfig = globalConfig;
        return this;
    }

    public InjectionConfig getCfg() {
        return injectionConfig;
    }

    public CustomAutoGenerator setCfg(InjectionConfig injectionConfig) {
        this.injectionConfig = injectionConfig;
        return this;
    }

    public Collection<DbTable> getDbTables() {
        return dbTables;
    }

    public CustomAutoGenerator setDbTables(Collection<DbTable> dbTables) {
        this.dbTables = dbTables;
        return this;
    }
}
