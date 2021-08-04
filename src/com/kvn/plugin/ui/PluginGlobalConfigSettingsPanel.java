package com.kvn.plugin.ui;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.kvn.plugin.presistentConfig.PersistentConfig;
import com.kvn.plugin.presistentConfig.PluginGlobalConfig;
import com.kvn.plugin.tools.CloneUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * 全局配置Ui
 * Created by wangzhiyuan on 2018/8/6
 */
public class PluginGlobalConfigSettingsPanel implements Configurable {
    /**
     * 主面板
     */
    private JPanel mainPanel;
    private JButton addItemButton;
    private JButton deleteItemButton;
    private JButton copyItemButton;
    private JPanel itemListPanel;
    private JPanel itemContentPanel;
    /**
     * 编辑框面板
     */
    private EditTemplatePanel editTemplatePanel;

    /************************用户配置的全局配置信息***************************/
    private List<PluginGlobalConfig> pluginGlobalConfigList;
    /**
     * 初始化标记
     */
    private boolean initFlag;
    /**
     * 当前选中的元素下标索引值
     */
    private int selectItemIndex;

    public PluginGlobalConfigSettingsPanel() {
        // 设置布局
        itemListPanel.setLayout(new VerticalFlowLayout());
        itemContentPanel.setLayout(new GridLayout());
        // 初始化事件
        initEvent();
        // 初始化
        init();
    }

    private void init() {
        initFlag = false;
        //初始化所有元素
        initItem();
        initFlag = true;
    }

    private void initEvent() {
        //添加元素事件
        addItemButton.addActionListener(e -> {
            if (!initFlag) {
                return;
            }
            String value = JOptionPane.showInputDialog(null, "Input Item Name:", "Demo");
            // 取消添加，不需要提示信息
            if (value == null) {
                return;
            }
            if (StringUtils.isEmpty(value)) {
                JOptionPane.showMessageDialog(null, "Item Name Can't Is Empty!");
                return;
            }
            List<PluginGlobalConfig> itemList = pluginGlobalConfigList;
            for (PluginGlobalConfig item : itemList) {
                if (item.getName().equals(value)) {
                    JOptionPane.showMessageDialog(null, "Item Name Already exist!");
                    return;
                }
            }
            itemList.add(new PluginGlobalConfig(value, "Demo!"));
            // 选中最后一个元素，即当前添加的元素
            selectItemIndex = itemList.size() - 1;
            initItem();
        });

        //删除元素
        deleteItemButton.addActionListener(e -> {
            if (!initFlag) {
                return;
            }
            List<PluginGlobalConfig> itemList = pluginGlobalConfigList;
            if (itemList.isEmpty()) {
                return;
            }
            String itemName = itemList.get(selectItemIndex).getName();
            int result = JOptionPane.showConfirmDialog(null, "Confirm Delete Item " + itemName + "?", "Title Info", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                itemList.remove(selectItemIndex);
                // 移步到当前删除元素的前一个元素
                if (selectItemIndex > 0) {
                    selectItemIndex--;
                }
                initItem();
            }
        });

        //复制元素
        copyItemButton.addActionListener(e -> {
            if (!initFlag) {
                return;
            }
            List<PluginGlobalConfig> itemList = pluginGlobalConfigList;
            if (itemList.isEmpty()) {
                return;
            }
            PluginGlobalConfig item = itemList.get(selectItemIndex);
            String itemName = item.getName();
            String value = JOptionPane.showInputDialog(null, "Input Item Name:", itemName + " Copy");
            // 取消复制，不需要提示信息
            if (value == null) {
                return;
            }
            if (value.trim().length() == 0) {
                JOptionPane.showMessageDialog(null, "Item Name Can't Is Empty!");
                return;
            }
            item = CloneUtils.instance().clone(item);
            // 设置元素名称
            item.setName(value);
            itemList.add(item);
            // 移步至当前复制的元素
            selectItemIndex = itemList.size() - 1;
            initItem();
        });
    }

    /**
     * 初始化所有元素
     */
    private void initItem() {
        initFlag = false;
        // 获取所有元素
        if (pluginGlobalConfigList == null) {
            pluginGlobalConfigList = CloneUtils.instance().cloneList(PersistentConfig.instance().getPluginGlobalConfigList());
        }
        List<PluginGlobalConfig> elementList = pluginGlobalConfigList;
        itemListPanel.removeAll();
        if (elementList.isEmpty()) {
            itemListPanel.updateUI();
            initFlag = true;
            return;
        }
        elementList.forEach(item -> {
            JButton button = new JButton();
            button.setText(item.getName());
            //元素选中事件
            button.addActionListener(e -> {

                if (!initFlag) {
                    return;
                }
                String itemName = button.getText();
                for (int i = 0; i < elementList.size(); i++) {
                    PluginGlobalConfig element = elementList.get(i);
                    if (itemName.equals(element.getName())) {
                        selectItemIndex = i;
                        initItemPanel(itemContentPanel, element);
                        return;
                    }
                }
            });
            itemListPanel.add(button);
        });
        itemListPanel.updateUI();
        // 修复下标越界异常
        if (selectItemIndex >= elementList.size()) {
            selectItemIndex = 0;
        }
        //初始化第一个元素面板
        initItemPanel(itemContentPanel, elementList.get(selectItemIndex));
        initFlag = true;
    }

    /**
     * 切换模板编辑时
     *
     * @param itemPanel 面板对象
     * @param item      模板对象
     */
    private void initItemPanel(JPanel itemPanel, PluginGlobalConfig item) {
        // 如果编辑面板已经实例化，需要选释放后再初始化
        if (editTemplatePanel != null) {
            editTemplatePanel.disposeEditor();
        }
        itemPanel.removeAll();
        editTemplatePanel = new EditTemplatePanel(item.getValue(), item::setValue);
        itemPanel.add(editTemplatePanel.getMainPanel());
        itemPanel.updateUI();
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Global Config";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return mainPanel;
    }

    @Override
    public boolean isModified() {
        // 当初始未完成时，插件进行修改判断
        if (editTemplatePanel != null) {
            editTemplatePanel.refresh();
        }
        return !PersistentConfig.instance().getPluginGlobalConfigList().equals(pluginGlobalConfigList);
    }

    @Override
    public void apply() throws ConfigurationException {
        // 提交保存时，不能将Pannel上的属性的引用直接赋值到PersistentConfig，否则 #isModified() 将失去意义
        PersistentConfig.instance().setPluginGlobalConfigList(CloneUtils.instance().cloneList(pluginGlobalConfigList));
    }
}
