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
 * ็ๆๆไปถ
 *
 * @author YangHu, tangguo
 * @since 2016-08-30
 */
public class CustomAutoGenerator {

    private static final Logger logger = LoggerFactory.getLogger(CustomAutoGenerator.class);

    protected CustomConfigBuilder config;
    protected InjectionConfig injectionConfig;

    /**
     * ๆฐๆฎๅบ่กจ้็ฝฎ
     */
    private StrategyConfig strategy;
    /**
     * ๅ ็ธๅณ้็ฝฎ
     */
    private PackageConfig packageInfo;
    /**
     * ๆจกๆฟ ็ธๅณ้็ฝฎ
     */
    private TemplateConfig template;

    /**
     * ๅจๅฑ ็ธๅณ้็ฝฎ
     */
    private GlobalConfig globalConfig;
    /**
     * velocityๅผๆ
     */
    private VelocityEngine engine;

    /**
     * ไปDatabaseๆไปถ้ขๆฟไธ่ทๅ็่กจไฟกๆฏ
     */
    private Collection<DbTable> dbTables;

    /**
     * ็ๆไปฃ็?
     */
    public void execute() {
        logger.debug("==========================ๅๅค็ๆๆไปถ...==========================");
        // ๅๅงๅ้็ฝฎ
        initConfig();
        // ๅๅปบ่พๅบๆไปถ่ทฏๅพ
        mkdirs(config.getPathInfo());
        // ่ทๅไธไธๆ
        Map<String, VelocityContext> ctxData = analyzeData(config);
        // ๅพช็ฏ็ๆๆไปถ
        for (Map.Entry<String, VelocityContext> ctx : ctxData.entrySet()) {
            batchOutput(ctx.getKey(), ctx.getValue());
        }

        openOutputPath();
        logger.debug("==========================ๆไปถ็ๆๅฎๆ๏ผ๏ผ๏ผ==========================");
    }

    /**
     * ๆๅผๆไปถ่พๅบ็ฎๅฝ
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
                        logger.debug("ๆไปถ่พๅบ็ฎๅฝ:" + config.getGlobalConfig().getOutputDir());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * <p>
     * ๅผๆพ่กจไฟกๆฏใ้ข็ๅญ็ฑป้ๅ
     * </p>
     *
     * @param config ้็ฝฎไฟกๆฏ
     * @return
     */
    protected List<TableInfo> getAllTableInfoList(CustomConfigBuilder config) {
        return config.getTableInfoList();
    }

    /**
     * <p>
     * ๅๆๆฐๆฎ
     * </p>
     *
     * @param config ๆป้็ฝฎไฟกๆฏ
     * @return ่งฃๆๆฐๆฎ็ปๆ้
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
                 * ๆณจๅฅ่ชๅฎไน้็ฝฎ
                 */
                injectionConfig.initMap();
                ctx.put("cfg", injectionConfig.getMap());
            }
            /* ---------- ๆทปๅ?ๅฏผๅฅๅ ---------- */
            if (config.getGlobalConfig().isActiveRecord()) {
                // ๅผๅฏ ActiveRecord ๆจกๅผ
                tableInfo.setImportPackages(Model.class.getCanonicalName());
            }
            if (tableInfo.isConvert()) {
                // ่กจๆณจ่งฃ
                tableInfo.setImportPackages(TableName.class.getCanonicalName());
            }
            if (tableInfo.isLogicDelete(config.getStrategyConfig().getLogicDeleteFieldName())) {
                // ้ป่พๅ?้คๆณจ่งฃ
                tableInfo.setImportPackages(TableLogic.class.getCanonicalName());
            }
            if (StringUtils.isNotEmpty(config.getStrategyConfig().getVersionFieldName())) {
                // ไน่ง้ๆณจ่งฃ
                tableInfo.setImportPackages(Version.class.getCanonicalName());
            }
            if (StringUtils.isNotEmpty(config.getSuperEntityClass())) {
                // ็ถๅฎไฝ
                tableInfo.setImportPackages(config.getSuperEntityClass());
            } else {
                tableInfo.setImportPackages(Serializable.class.getCanonicalName());
            }
            // Boolean็ฑปๅisๅ็ผๅค็
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
            // RequestMapping ่ฟๅญ็ฌฆ้ฃๆ?ผ user-info
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

            /*********่ชๅฎไนๆฉๅฑ๏ผๆทปๅ?ๅจๅฑ้็ฝฎ************/
            PersistentConfig.instance().getPluginGlobalConfigList().forEach(data -> {
                ctx.put(data.getName(), data.getValue());
            });


            ctxData.put(tableInfo.getEntityName(), ctx);
        }



        return ctxData;
    }

    /**
     * <p>
     * ่ทๅ็ฑปๅ
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
     * ๅค็่พๅบ็ฎๅฝ
     * </p>
     *
     * @param pathInfo ่ทฏๅพไฟกๆฏ
     */
    private void mkdirs(Map<String, String> pathInfo) {
        for (Map.Entry<String, String> entry : pathInfo.entrySet()) {
            File dir = new File(entry.getValue());
            if (!dir.exists()) {
                boolean result = dir.mkdirs();
                if (result) {
                    logger.debug("ๅๅปบ็ฎๅฝ๏ผ [" + entry.getValue() + "]");
                }
            }
        }
    }

    /**
     * <p>
     * ๅๆไธไธๆไธๆจกๆฟ
     * </p>
     *
     * @param context vmไธไธๆ
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

            // ๆ?นๆฎoverrideๆ?่ฏๆฅๅคๆญๆฏๅฆ้่ฆๅๅปบๆไปถ
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
                 * ่พๅบ่ชๅฎไนๆไปถๅๅฎน
                 */
                List<FileOutConfig> focList = injectionConfig.getFileOutConfigList();
                if (CollectionUtils.isNotEmpty(focList)) {
                    for (FileOutConfig foc : focList) {
                        // ๅคๆญ่ชๅฎไนๆไปถๆฏๅฆๅญๅจ
                        if (isCreate(foc.outputFile(tableInfo))) {
                            vmToFile(context, foc.getTemplatePath(), foc.outputFile(tableInfo), templateImmutableMap);
                        }
                    }
                }
            }

        } catch (IOException e) {
            logger.error("ๆ?ๆณๅๅปบๆไปถ๏ผ่ฏทๆฃๆฅ้็ฝฎไฟกๆฏ๏ผ", e);
        }
    }

    /**
     * ๆไปถๅ็ผ
     */
    protected String suffixJavaOrKt() {
        return config.getGlobalConfig().isKotlin() ? ConstVal.KT_SUFFIX : ConstVal.JAVA_SUFFIX;
    }

    /**
     * <p>
     * ๅฐๆจกๆฟ่ฝฌๅๆไธบๆไปถ
     * </p>
     *  @param context      ๅๅฎนๅฏน่ฑก
     * @param templatePath ๆจกๆฟๆไปถ
     * @param outputFile   ๆไปถ็ๆ็็ฎๅฝ
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
            // ๅฆๆๆไปถๆๅจ็็ฎๅฝไธๅญๅจ๏ผๅๅๅปบ็ฎๅฝ
            if (!file.getParentFile().mkdirs()) {
                logger.debug("ๅๅปบๆไปถๆๅจ็็ฎๅฝๅคฑ่ดฅ!");
                return;
            }
        }

        FileUtils.getInstance().write(file, content);

        logger.debug("ๆจกๆฟ:" + templatePath + ";  ๆไปถ:" + outputFile);
    }

    /**
     * ่ฎพ็ฝฎๆจก็ๅผๆ๏ผไธป่ฆๆๅ่ทๅๆจก็่ทฏๅพ
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
     * ๆฃๆตๆไปถๆฏๅฆๅญๅจ
     *
     * @return ๆฏๅฆ
     */
    private boolean isCreate(String filePath) {
        File file = new File(filePath);
        return !file.exists() || config.getGlobalConfig().isFileOverride();
    }

    // ==================================  ็ธๅณ้็ฝฎ  ==================================

    /**
     * ๅๅงๅ้็ฝฎ
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
