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
package com.baomidou.mybatisplus.generator.config;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.kvn.plugin.Debugger;
import com.kvn.plugin.config.Template;
import com.kvn.plugin.config.TemplateGroup;

import java.util.List;

/**
 * <p>
 * 模板路径配置项
 * </p>
 *
 * @author tzg hubin
 * @since 2017-06-17
 */
public class TemplateConfig {

    private String entity = ConstVal.TEMPLATE_ENTITY_JAVA;

    private String service = ConstVal.TEMPLATE_SERVICE;

    private String serviceImpl = ConstVal.TEMPLATE_SERVICEIMPL;

    private String mapper = ConstVal.TEMPLATE_MAPPER;

    private String xml = ConstVal.TEMPLATE_XML;

    private String controller = ConstVal.TEMPLATE_CONTROLLER;


    public static TemplateConfig getDefaultTemplateConfig(){
        return new TemplateConfig("default-MyBatisPlus", null);
    }

    /**
     * 初始化模板路径。如果初始化后，模板路径为null，则不使用该模板生成代码
     * @param templateGroup
     * @param selectTemplateList 为null，则默认使用全部模板
     */
    public TemplateConfig(String templateGroup, List<Template> selectTemplateList) {
        if (selectTemplateList == null) {
            selectTemplateList = TemplateGroup.cloneTemplateGroupMapFromPersitentFile().get(templateGroup).getElementList();
        }

        List<String> selectTemplateNames = Lists.transform(selectTemplateList, new Function<Template, String>() {
            public String apply(Template template) {
                return template.getName();
            }
        });
//        Debugger.debug(selectTemplateList);
        entity = resetTemplate(entity, selectTemplateNames, templateGroup);
        service = resetTemplate(service, selectTemplateNames, templateGroup);
        serviceImpl = resetTemplate(serviceImpl, selectTemplateNames, templateGroup);
        mapper = resetTemplate(mapper, selectTemplateNames, templateGroup);
        xml = resetTemplate(xml, selectTemplateNames, templateGroup);
        controller = resetTemplate(controller, selectTemplateNames, templateGroup);
//        Debugger.debug(entity);
    }

    private String resetTemplate(String templatePath, List<String> selectTemplateNames, String templateGroup) {
        String templateName = templatePath.substring(templatePath.lastIndexOf("/") + 1);
        String templateNameWithoutVm = templateName.substring(0, templateName.length() - 3); // FIXME ???
        if (selectTemplateNames.contains(templateName) || selectTemplateNames.contains(templateNameWithoutVm)) {
            return String.format(templatePath, templateGroup);
        }
        return null;
    }


    public String getEntity(boolean kotlin) {
        return kotlin ? ConstVal.TEMPLATE_ENTITY_KT : entity;
    }

    public TemplateConfig setEntity(String entity) {
        this.entity = entity;
        return this;
    }

    public String getService() {
        return service;
    }

    public TemplateConfig setService(String service) {
        this.service = service;
        return this;
    }

    public String getServiceImpl() {
        return serviceImpl;
    }

    public TemplateConfig setServiceImpl(String serviceImpl) {
        this.serviceImpl = serviceImpl;
        return this;
    }

    public String getMapper() {
        return mapper;
    }

    public TemplateConfig setMapper(String mapper) {
        this.mapper = mapper;
        return this;
    }

    public String getXml() {
        return xml;
    }

    public TemplateConfig setXml(String xml) {
        this.xml = xml;
        return this;
    }

    public String getController() {
        return controller;
    }

    public TemplateConfig setController(String controller) {
        this.controller = controller;
        return this;
    }

}
