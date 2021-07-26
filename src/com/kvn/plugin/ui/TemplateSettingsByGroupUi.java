package com.kvn.plugin.ui;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.kvn.plugin.config.Template;
import com.kvn.plugin.config.TemplateGroup;
import com.kvn.plugin.presistentConfig.PersistentConfig;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Created by wangzhiyuan on 2018/8/3
 */
public class TemplateSettingsByGroupUi extends SettingsByGroupUi<TemplateGroup, Template> implements Configurable {

    /**
     * 编辑框面板
     */
    private EditTemplatePanel editTemplatePanel;

    public TemplateSettingsByGroupUi() {
        super(TemplateGroup.cloneTemplateGroupMapFromPersitentFile(), PersistentConfig.instance().getCurrTemplateGroupName());
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Template Setting";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return this.mainPannel;
    }

    /**
     * 配置是否修改过
     *
     * @return 是否修改过
     */
    @Override
    public boolean isModified() {
        // 当初始未完成时，插件进行修改判断
        if (editTemplatePanel!=null) {
            editTemplatePanel.refresh();
        }
        return !PersistentConfig.instance().getTemplateGroupMap().equals(settingsGroupMap) || !PersistentConfig.instance().getCurrTemplateGroupName().equals(currGroupName);
    }

    /**
     * Settings上的Apply按钮（保存方法）
     */
    @Override
    public void apply() throws ConfigurationException {
        PersistentConfig.instance().setCurrTemplateGroupName(currGroupName);
        PersistentConfig.instance().setTemplateGroupMap(settingsGroupMap);
    }

    @Override
    protected void initItemContentPanel(JPanel itemContentPannel, Template item) {
        // 如果编辑面板已经实例化，需要选释放后再初始化
        if (editTemplatePanel != null) {
            editTemplatePanel.disposeEditor();
        }
        itemContentPannel.removeAll();
        editTemplatePanel = new EditTemplatePanel(item.getCode(), item::setCode);
        itemContentPannel.add(editTemplatePanel.getMainPanel());
        itemContentPannel.updateUI();
    }

    @Override
    protected String getItemName(Template item) {
        return item.getName();
    }

    @Override
    protected void setItemName(Template item, String itemName) {
        item.setName(itemName);
    }

    @Override
    protected Template createItem(String name) {
        return new Template(name, "Demo!");
    }
}
