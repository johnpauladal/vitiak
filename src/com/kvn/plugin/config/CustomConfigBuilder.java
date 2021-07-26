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
package com.kvn.plugin.config;

import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.converts.MySqlTypeConvert;
import com.baomidou.mybatisplus.generator.config.po.TableField;
import com.baomidou.mybatisplus.generator.config.po.TableFill;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.baomidou.mybatisplus.generator.config.rules.DbType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.config.rules.QuerySQL;
import com.baomidou.mybatisplus.toolkit.StringUtils;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.intellij.database.model.DasColumn;
import com.intellij.database.psi.DbTable;
import com.intellij.database.util.DasUtil;
import com.intellij.util.containers.JBIterable;
import com.kvn.plugin.KvnPluginContext;

import java.io.File;
import java.util.*;

/**
 * <p>
 * 配置汇总 传递给文件生成工具
 * </p>
 *
 * @author YangHu, tangguo, hubin
 * @since 2016-08-30
 */
public class CustomConfigBuilder {

    /**
     * 模板路径配置信息
     */
    private final TemplateConfig template;
    private final TemplateGroup templateGroup;

    private String superEntityClass;
    private String superMapperClass;
    /**
     * service超类定义
     */
    private String superServiceClass;
    private String superServiceImplClass;
    private String superControllerClass;
    /**
     * 数据库表信息
     */
    private List<TableInfo> tableInfoList;
    /**
     * 包配置详情
     */
    private Map<String, String> packageInfo;
    /**
     * 路径配置信息
     */
    private Map<String, String> pathInfo;
    /**
     * 策略配置
     */
    private StrategyConfig strategyConfig;

    /**
     * 全局配置信息
     */
    private GlobalConfig globalConfig;

    // FIXME 根据DB来选择
    private ITypeConvert typeConvert = new MySqlTypeConvert();

    /**
     * <p>
     * 在构造器中处理配置
     * </p>
     *
     * @param packageConfig    包配置
     * @param strategyConfig   表配置
     * @param template         模板配置
     * @param globalConfig     全局配置
     */
    public CustomConfigBuilder(PackageConfig packageConfig, StrategyConfig strategyConfig,
                               TemplateConfig template, GlobalConfig globalConfig, Collection<DbTable> dbTables) {
        // 全局配置
        if (null == globalConfig) {
            this.globalConfig = new GlobalConfig();
        } else {
            this.globalConfig = globalConfig;
        }
        this.templateGroup = KvnPluginContext.instance().getSelectedTemplateGroup();
        // 模板配置
        if (null == template) {
            this.template = new TemplateConfig(templateGroup.getName(), null);
        } else {
            this.template = template;
        }

        // 包配置
        if (null == packageConfig) {
            handlerPackage(this.template, this.globalConfig.getOutputDir(), new PackageConfig());
        } else {
            handlerPackage(this.template, this.globalConfig.getOutputDir(), packageConfig);
        }

        // 策略配置
        if (null == strategyConfig) {
            this.strategyConfig = new StrategyConfig();
        } else {
            this.strategyConfig = strategyConfig;
        }
        handlerStrategy(dbTables);
    }

    // ************************ 曝露方法 BEGIN*****************************

    /**
     * <p>
     * 所有包配置信息
     * </p>
     *
     * @return 包配置
     */
    public Map<String, String> getPackageInfo() {
        return packageInfo;
    }

    /**
     * <p>
     * 所有路径配置
     * </p>
     *
     * @return 路径配置
     */
    public Map<String, String> getPathInfo() {
        return pathInfo;
    }

    public String getSuperEntityClass() {
        return superEntityClass;
    }

    public String getSuperMapperClass() {
        return superMapperClass;
    }

    /**
     * <p>
     * 获取超类定义
     * </p>
     *
     * @return 完整超类名称
     */
    public String getSuperServiceClass() {
        return superServiceClass;
    }

    public String getSuperServiceImplClass() {
        return superServiceImplClass;
    }

    public String getSuperControllerClass() {
        return superControllerClass;
    }

    /**
     * <p>
     * 表信息
     * </p>
     *
     * @return 所有表信息
     */
    public List<TableInfo> getTableInfoList() {
        return tableInfoList;
    }

    /**
     * <p>
     * 模板路径配置信息
     * </p>
     *
     * @return 所以模板路径配置信息
     */
    public TemplateConfig getTemplate() {
        return template == null ? new TemplateConfig(templateGroup.getName(), null) : template;
    }

    // ****************************** 曝露方法 END**********************************

    /**
     * <p>
     * 处理包配置
     * </p>
     *
     * @param template  TemplateConfig
     * @param outputDir
     * @param config    PackageConfig
     */
    private void handlerPackage(TemplateConfig template, String outputDir, PackageConfig config) {
        packageInfo = new HashMap<>();
        packageInfo.put(ConstVal.MODULENAME, config.getModuleName());
        packageInfo.put(ConstVal.ENTITY, joinPackage(config.getParent(), config.getEntity()));
        packageInfo.put(ConstVal.MAPPER, joinPackage(config.getParent(), config.getMapper()));
        packageInfo.put(ConstVal.XML, joinPackage(config.getParent(), config.getXml()));
        packageInfo.put(ConstVal.SERIVCE, joinPackage(config.getParent(), config.getService()));
        packageInfo.put(ConstVal.SERVICEIMPL, joinPackage(config.getParent(), config.getServiceImpl()));
        packageInfo.put(ConstVal.CONTROLLER, joinPackage(config.getParent(), config.getController()));

        // 生成路径信息
        pathInfo = new HashMap<>();
        if (StringUtils.isNotEmpty(template.getEntity(getGlobalConfig().isKotlin()))) {
            pathInfo.put(ConstVal.ENTITY_PATH, joinPath(outputDir, packageInfo.get(ConstVal.ENTITY)));
        }
        if (StringUtils.isNotEmpty(template.getMapper())) {
            pathInfo.put(ConstVal.MAPPER_PATH, joinPath(outputDir, packageInfo.get(ConstVal.MAPPER)));
        }
        if (StringUtils.isNotEmpty(template.getXml())) {
            pathInfo.put(ConstVal.XML_PATH, joinPath(outputDir, packageInfo.get(ConstVal.XML)));
        }
        if (StringUtils.isNotEmpty(template.getService())) {
            pathInfo.put(ConstVal.SERIVCE_PATH, joinPath(outputDir, packageInfo.get(ConstVal.SERIVCE)));
        }
        if (StringUtils.isNotEmpty(template.getServiceImpl())) {
            pathInfo.put(ConstVal.SERVICEIMPL_PATH, joinPath(outputDir, packageInfo.get(ConstVal.SERVICEIMPL)));
        }
        if (StringUtils.isNotEmpty(template.getController())) {
            pathInfo.put(ConstVal.CONTROLLER_PATH, joinPath(outputDir, packageInfo.get(ConstVal.CONTROLLER)));
        }
    }


    /**
     * <p>
     * 处理数据库表 加载数据库表、列、注释相关数据集
     * </p>
     *
     */
    private void handlerStrategy(Collection<DbTable> dbTables) {
        processTypes(strategyConfig);
        tableInfoList = getTablesInfo(dbTables);
    }

    /**
     * <p>
     * 处理superClassName,IdClassType,IdStrategy配置
     * </p>
     *
     * @param config 策略配置
     */
    private void processTypes(StrategyConfig config) {
        if (StringUtils.isEmpty(config.getSuperServiceClass())) {
            superServiceClass = ConstVal.SUPERD_SERVICE_CLASS;
        } else {
            superServiceClass = config.getSuperServiceClass();
        }
        if (StringUtils.isEmpty(config.getSuperServiceImplClass())) {
            superServiceImplClass = ConstVal.SUPERD_SERVICEIMPL_CLASS;
        } else {
            superServiceImplClass = config.getSuperServiceImplClass();
        }
        if (StringUtils.isEmpty(config.getSuperMapperClass())) {
            superMapperClass = ConstVal.SUPERD_MAPPER_CLASS;
        } else {
            superMapperClass = config.getSuperMapperClass();
        }
        superEntityClass = config.getSuperEntityClass();
        superControllerClass = config.getSuperControllerClass();
    }

    /**
     * <p>
     * 处理表对应的类名称
     * </P>
     *
     * @param tableList 表名称
     * @param strategy  命名策略
     * @param config    策略配置项
     * @return 补充完整信息后的表
     */
    private List<TableInfo> processTable(List<TableInfo> tableList, NamingStrategy strategy, StrategyConfig config) {
        String[] tablePrefix = config.getTablePrefix();
        String[] fieldPrefix = config.getFieldPrefix();
        for (TableInfo tableInfo : tableList) {
            tableInfo.setEntityName(strategyConfig, NamingStrategy.capitalFirst(processName(tableInfo.getName(), strategy, tablePrefix)));
            if (StringUtils.isNotEmpty(globalConfig.getMapperName())) {
                tableInfo.setMapperName(String.format(globalConfig.getMapperName(), tableInfo.getEntityName()));
            } else {
                tableInfo.setMapperName(tableInfo.getEntityName() + ConstVal.MAPPER);
            }
            if (StringUtils.isNotEmpty(globalConfig.getXmlName())) {
                tableInfo.setXmlName(String.format(globalConfig.getXmlName(), tableInfo.getEntityName()));
            } else {
                tableInfo.setXmlName(tableInfo.getEntityName() + ConstVal.MAPPER);
            }
            if (StringUtils.isNotEmpty(globalConfig.getServiceName())) {
                tableInfo.setServiceName(String.format(globalConfig.getServiceName(), tableInfo.getEntityName()));
            } else {
                tableInfo.setServiceName("I" + tableInfo.getEntityName() + ConstVal.SERIVCE);
            }
            if (StringUtils.isNotEmpty(globalConfig.getServiceImplName())) {
                tableInfo.setServiceImplName(String.format(globalConfig.getServiceImplName(), tableInfo.getEntityName()));
            } else {
                tableInfo.setServiceImplName(tableInfo.getEntityName() + ConstVal.SERVICEIMPL);
            }
            if (StringUtils.isNotEmpty(globalConfig.getControllerName())) {
                tableInfo.setControllerName(String.format(globalConfig.getControllerName(), tableInfo.getEntityName()));
            } else {
                tableInfo.setControllerName(tableInfo.getEntityName() + ConstVal.CONTROLLER);
            }
            //强制开启字段注解
            checkTableIdTableFieldAnnotation(config, tableInfo, fieldPrefix);
        }
        return tableList;
    }

    /**
     * 检查是否有
     *     {@link com.baomidou.mybatisplus.annotations.TableId}
     *  {@link com.baomidou.mybatisplus.annotations.TableField}
     *  注解
     * @param config
     * @param tableInfo
     * @param fieldPrefix
     */
    private void checkTableIdTableFieldAnnotation(StrategyConfig config, TableInfo tableInfo, String[] fieldPrefix){
        boolean importTableFieldAnnotaion = false;
        boolean importTableIdAnnotaion = false;
        if (config.isEntityTableFieldAnnotationEnable()) {
            for (TableField tf : tableInfo.getFields()) {
                tf.setConvert(true);
                importTableFieldAnnotaion = true;
                importTableIdAnnotaion = true;
            }
        } else if (fieldPrefix != null && fieldPrefix.length != 0) {
            for (TableField tf : tableInfo.getFields()) {
                if (NamingStrategy.isPrefixContained(tf.getName(), fieldPrefix)) {
                    if (tf.isKeyFlag()) {
                        importTableIdAnnotaion = true;
                    }
                    tf.setConvert(true);
                    importTableFieldAnnotaion = true;
                }
            }
        }
        if (importTableFieldAnnotaion) {
            tableInfo.getImportPackages().add(com.baomidou.mybatisplus.annotations.TableField.class.getCanonicalName());
        }
        if (importTableIdAnnotaion) {
            tableInfo.getImportPackages().add(com.baomidou.mybatisplus.annotations.TableId.class.getCanonicalName());
        }
        if(globalConfig.getIdType()!=null){
            if(!importTableIdAnnotaion){
                tableInfo.getImportPackages().add(com.baomidou.mybatisplus.annotations.TableId.class.getCanonicalName());
            }
            tableInfo.getImportPackages().add(com.baomidou.mybatisplus.enums.IdType.class.getCanonicalName());
        }
    }


    /**
     * 数据库表处理器
     *
     * @param dbTables   数据库表
     * @return 处理结果
     */
    private List<TableInfo> getTablesInfo(Collection<DbTable> dbTables) {
        String[] tableNames = Collections2.transform(dbTables, new Function<DbTable, String>() {
            public String apply(DbTable dbTable) {
                return dbTable.getName();
            }
        }).toArray(new String[]{});
        // FIXME 设置StrategyConfig，后面可以做与配置页面
        strategyConfig = new StrategyConfig()
                .setCapitalMode(true)
                .setEntityLombokModel(true) // lombok支持
                .setDbColumnUnderline(true)
                .setNaming(NamingStrategy.underline_to_camel)
//                .setTablePrefix("ts_")
                .setInclude(tableNames);//修改替换成你需要的表名，多个表名传数组


        List<TableInfo> result = new ArrayList<>();
        dbTables.forEach(dbTable -> {
            result.add(convertTableFields(dbTable));
        });

        return processTable(result, strategyConfig.getNaming(), strategyConfig);
    }

    /**
     * <p>
     * 将字段信息与表信息关联
     * </p>
     *
     * @param dbTable 表信息
     * @return
     * @see com.baomidou.mybatisplus.generator.config.builder.ConfigBuilder#convertTableFields
     */
    private TableInfo convertTableFields(DbTable dbTable) {
        TableInfo tableInfo = new TableInfo();
        tableInfo.setName(dbTable.getName());
        tableInfo.setComment(dbTable.getComment());
        tableInfo.setEntityName(strategyConfig, NamingStrategy.capitalFirst(processName(tableInfo.getName(), strategyConfig.getNaming(), null)));


        List<TableField> fieldList = new ArrayList<>();
        List<TableField> commonFieldList = new ArrayList<>();


        // 处理所有列
        JBIterable<? extends DasColumn> columns = DasUtil.getColumns(dbTable);
        for (DasColumn column : columns) {
            TableField field = new TableField();
            field.setKeyFlag(DasUtil.isPrimary(column)); // 是否为主键
            field.setKeyIdentityFlag(DasUtil.isAutoGenerated(column)); // 主键是否为自增类型
            // 处理其它信息
            field.setName(column.getName());
            field.setType(column.getDataType().typeName);
            field.setPropertyName(strategyConfig, processName(field.getName(), strategyConfig.getNaming()));
            field.setColumnType(typeConvert.processTypeConvert(field.getType()));
            field.setComment(column.getComment());
            if (strategyConfig.includeSuperEntityColumns(field.getName())) {
                // 跳过公共字段
                commonFieldList.add(field);
                continue;
            }
            // 填充逻辑判断
            List<TableFill> tableFillList = strategyConfig.getTableFillList();
            if (null != tableFillList) {
                for (TableFill tableFill : tableFillList) {
                    if (tableFill.getFieldName().equals(field.getName())) {
                        field.setFill(tableFill.getFieldFill().name());
                        break;
                    }
                }
            }
            fieldList.add(field);
        }
        tableInfo.setFields(fieldList);
        tableInfo.setCommonFields(commonFieldList);
        return tableInfo;
    }


    /**
     * <p>
     * 连接路径字符串
     * </p>
     *
     * @param parentDir   路径常量字符串
     * @param packageName 包名
     * @return 连接后的路径
     */
    private String joinPath(String parentDir, String packageName) {
        if (StringUtils.isEmpty(parentDir)) {
            parentDir = System.getProperty(ConstVal.JAVA_TMPDIR);
        }
        if (!StringUtils.endsWith(parentDir, File.separator)) {
            parentDir += File.separator;
        }
        packageName = packageName.replaceAll("\\.", "\\" + File.separator);
        return parentDir + packageName;
    }

    /**
     * <p>
     * 连接父子包名
     * </p>
     *
     * @param parent     父包名
     * @param subPackage 子包名
     * @return 连接后的包名
     */
    private String joinPackage(String parent, String subPackage) {
        if (StringUtils.isEmpty(parent)) {
            return subPackage;
        }
        return parent + "." + subPackage;
    }

    /**
     * <p>
     * 处理字段名称
     * </p>
     *
     * @return 根据策略返回处理后的名称
     */
    private String processName(String name, NamingStrategy strategy) {
        return processName(name, strategy, this.strategyConfig.getFieldPrefix());
    }

    /**
     * <p>
     * 处理表/字段名称
     * </p>
     *
     * @param name
     * @param strategy
     * @param prefix
     * @return 根据策略返回处理后的名称
     */
    private String processName(String name, NamingStrategy strategy, String[] prefix) {
        boolean removePrefix = false;
        if (prefix != null && prefix.length >= 1) {
            removePrefix = true;
        }
        String propertyName;
        if (removePrefix) {
            if (strategy == NamingStrategy.underline_to_camel) {
                // 删除前缀、下划线转驼峰
                propertyName = NamingStrategy.removePrefixAndCamel(name, prefix);
            } else {
                // 删除前缀
                propertyName = NamingStrategy.removePrefix(name, prefix);
            }
        } else if (strategy == NamingStrategy.underline_to_camel) {
            // 下划线转驼峰
            propertyName = NamingStrategy.underlineToCamel(name);
        } else {
            // 不处理
            propertyName = name;
        }
        return propertyName;
    }

    /**
     * 获取当前的SQL类型
     *
     * @return DB类型
     */
    private QuerySQL getQuerySQL(DbType dbType) {
        for (QuerySQL qs : QuerySQL.values()) {
            if (qs.getDbType().equals(dbType.getValue())) {
                return qs;
            }
        }
        return QuerySQL.MYSQL;
    }

    public StrategyConfig getStrategyConfig() {
        return strategyConfig;
    }

    public CustomConfigBuilder setStrategyConfig(StrategyConfig strategyConfig) {
        this.strategyConfig = strategyConfig;
        return this;
    }

    public GlobalConfig getGlobalConfig() {
        return globalConfig;
    }

    public CustomConfigBuilder setGlobalConfig(GlobalConfig globalConfig) {
        this.globalConfig = globalConfig;
        return this;
    }

}
