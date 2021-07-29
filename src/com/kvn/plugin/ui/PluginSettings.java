package com.kvn.plugin.ui;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.UnnamedConfigurable;
import com.kvn.plugin.KvnPluginContext;
import com.kvn.plugin.presistentConfig.PersistentConfig;
import com.kvn.plugin.tools.CollectionUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangzhiyuan on 2018/8/3
 */
public class PluginSettings implements Configurable, Configurable.Composite {
    private JPanel mainPanel;
    /**
     * 代码作者输入框
     */
    private JTextField authorTextField;
    /**
     * 重置所有配置按钮
     */
    private JButton resetAllButton;
    /**
     * 重置列表
     */
    private List<Configurable> resetList;
    /**
     * 需要保存的列表
     */
    private List<Configurable> saveList;


    @Override
    public void reset() {
        initUi();
    }

    public PluginSettings() {
        initUi();
    }

    private void initUi() {
        // 初始化数据
        authorTextField.setText(PersistentConfig.instance().getAuthor());

        //重置配置信息
        resetAllButton.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(null, "确认重置默认配置?\n重置默认配置只会还原插件自带分组配置信息，不会删除用户新增分组信息。", "Title Info", JOptionPane.OK_CANCEL_OPTION);
            if (JOptionPane.YES_OPTION == result) {
                if (CollectionUtil.isEmpty(resetList)) {
                    return;
                }
                // 重置默认配置
                PersistentConfig.instance().doInit();
                resetList.forEach(UnnamedConfigurable::reset);
                if (CollectionUtil.isEmpty(saveList)) {
                    return;
                }
                saveList.forEach(configurable -> {
                    try {
                        configurable.apply();
                    } catch (ConfigurationException e1) {
                        e1.printStackTrace();
                    }
                });
            }
        });
    }

    /**
     * 配置标题名称
     * @return
     */
    @Nls
    @Override
    public String getDisplayName() {
        return "KvnCode";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return this.mainPanel;
    }

    @Override
    public boolean isModified() {
        return !PersistentConfig.instance().getAuthor().equals(authorTextField.getText());
    }

    /**
     * 修改提交
     * @throws ConfigurationException
     */
    @Override
    public void apply() throws ConfigurationException {
        PersistentConfig.instance().setAuthor(authorTextField.getText());
    }

    /**
     * 更多配置
     * @return 配置选项
     */
    @NotNull
    @Override
    public Configurable[] getConfigurables() {
        // 初始化上下文
        KvnPluginContext.initAfterSettingsPop();

        Configurable[] result = new Configurable[2];
        result[0] = new TemplateSettingsByGroupUi();
        result[1] = new PluginGlobalConfigSettingsPanel();
//        // 需要重置的配置
//        resetList = new ArrayList<>();
//        resetList.add(result[0]);
//
//        // 不需要重置的配置
//        saveList = new ArrayList<>();
//        saveList.add(this);

        return result;
    }
}
