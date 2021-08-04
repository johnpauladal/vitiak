package com.kvn.plugin.ui;

import com.intellij.openapi.ui.VerticalFlowLayout;
import com.kvn.plugin.config.AbstractGroup;
import com.kvn.plugin.config.TemplateGroupEnum;
import com.kvn.plugin.tools.CloneUtils;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * 按照分组来设置的配置项公共Ui
 * Created by wangzhiyuan on 2018/8/3
 */
public abstract class SettingsByGroupUi<T extends AbstractGroup<E>, E>{
    /**
     * 主面板
     */
    protected JPanel mainPannel;
    /**
     * 分组下拉选择框
     */
    private JComboBox groupComboBox;
    /**
     * 复制按钮
     */
    private JButton copyGroupButton;
    /**
     * 删除按钮
     */
    private JButton deleteGroupButton;
    /**
     * 新增条目按钮
     */
    private JButton addItemButton;
    /**
     * 删除条目按钮
     */
    private JButton deleteItemButton;
    /**
     * 复制条目按钮
     */
    private JButton copyItemButton;
    /**
     * 条目列表面板
     */
    private JPanel itemListPannel;
    /**
     * 条目内容面板
     */
    private JPanel itemContentPannel;

    /***************************************/
    /**
     * 初始化标记
     */
    protected boolean initFlag;
    /**
     * 当前选中的元素下标索引值
     */
    protected int selectItemIndex;
    /**
     * 当前分组名称
     */
    protected String currGroupName;
    /**
     * 按分组的配置map
     */
    protected Map<String, T> settingsGroupMap;

    public SettingsByGroupUi(Map<String, T> settingsGroupMap, String selectGroupName) {
        this.settingsGroupMap = settingsGroupMap;
        this.currGroupName = selectGroupName;
        initUi();
        init();
    }

    protected void init() {
        initFlag = false;
        //初始化所有组
        initGroup();
        //初始化所有元素
        initItem();
        initFlag = true;
    }

    /**
     * 初始化分组
     */
    private void initGroup() {
        groupComboBox.removeAllItems();
        for (String groupName : settingsGroupMap.keySet()) {
            groupComboBox.addItem(groupName);
        }
        // 设置选中默认分组
        groupComboBox.setSelectedItem(currGroupName);
    }

    /**
     * 初始化所有元素
     */
    private void initItem() {
        initFlag = false;
        //获取选中组的所有元素
        List<E> elementList = settingsGroupMap.get(currGroupName).getElementList();
        itemListPannel.removeAll();
        if (elementList.isEmpty()) {
            itemListPannel.updateUI();
            initFlag = true;
            return;
        }
        elementList.forEach(item -> {
            JButton button = new JButton();
            button.setText(getItemName(item));
            //元素选中事件
            button.addActionListener(e -> {

                if (!initFlag) {
                    return;
                }
                String itemName = button.getText();
                for (int i = 0; i < elementList.size(); i++) {
                    E element = elementList.get(i);
                    if (itemName.equals(getItemName(element))) {
                        selectItemIndex = i;
                        initItemContentPanel(itemContentPannel, element);
                        return;
                    }
                }
            });
            itemListPannel.add(button);
        });
        itemListPannel.updateUI();
        // 修复下标越界异常
        if (selectItemIndex >= elementList.size()) {
            selectItemIndex = 0;
        }
        //初始化第一个元素面板
        initItemContentPanel(itemContentPannel, elementList.get(selectItemIndex));
        initFlag = true;
    }


    private void initUi() {
        // 设置布局
        itemListPannel.setLayout(new VerticalFlowLayout());
        itemContentPannel.setLayout(new GridLayout());

        //切换分组事件
        groupComboBox.addActionListener(e -> {
            // 未初始化完成禁止切换分组
            if (!initFlag) {
                return;
            }
            String groupName = (String) groupComboBox.getSelectedItem();
            if (StringUtils.isEmpty(groupName)) {
                return;
            }
            if (currGroupName.equals(groupName)) {
                return;
            }
            this.currGroupName = groupName;
            init();
        });

        //复制分组事件
        copyGroupButton.addActionListener(e -> {
            // 未初始化禁止复制分组
            if (!initFlag) {
                return;
            }
            // 输入分组名称
            String value = JOptionPane.showInputDialog(null, "Input Group Name:", currGroupName + " Copy");

            // 取消复制，不需要提示信息
            if (value == null) {
                return;
            }

            if (StringUtils.isEmpty(value)) {
                JOptionPane.showMessageDialog(null, "Group Name Can't Is Empty!");
                return;
            }
            if (settingsGroupMap.containsKey(value)) {
                JOptionPane.showMessageDialog(null, "Group Name Already exist!");
                return;
            }
            // 克隆对象
            T groupItem = CloneUtils.instance().clone(settingsGroupMap.get(currGroupName));
            groupItem.setName(value);
            settingsGroupMap.put(value, groupItem);
            currGroupName = value;
            init();
        });

        //删除分组事件
        deleteGroupButton.addActionListener(e -> {
            if (!initFlag) {
                return;
            }
            int result = JOptionPane.showConfirmDialog(null, "Confirm Delete Group " + currGroupName + "?", "Title Info", JOptionPane.OK_CANCEL_OPTION);
            // 点击YES选项时
            if (JOptionPane.YES_OPTION == result) {
                if (TemplateGroupEnum.DEFAULT_GROUP_NAME.getPathName().equals(currGroupName)) {
                    JOptionPane.showMessageDialog(null, "Can't Delete Default Group!");
                    return;
                }
                settingsGroupMap.remove(currGroupName);
                currGroupName = TemplateGroupEnum.DEFAULT_GROUP_NAME.getPathName();
                init();
            }
        });

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
            java.util.List<E> itemList = settingsGroupMap.get(currGroupName).getElementList();
            for (E item : itemList) {
                if (getItemName(item).equals(value)) {
                    JOptionPane.showMessageDialog(null, "Item Name Already exist!");
                    return;
                }
            }
            itemList.add(createItem(value));
            // 选中最后一个元素，即当前添加的元素
            selectItemIndex = itemList.size() - 1;
            initItem();
        });

        //删除元素
        deleteItemButton.addActionListener(e -> {
            if (!initFlag) {
                return;
            }
            java.util.List<E> itemList = settingsGroupMap.get(currGroupName).getElementList();
            if (itemList.isEmpty()) {
                return;
            }
            String itemName = getItemName(itemList.get(selectItemIndex));
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
            List<E> itemList = settingsGroupMap.get(currGroupName).getElementList();
            if (itemList.isEmpty()) {
                return;
            }
            E item = itemList.get(selectItemIndex);
            String itemName = getItemName(item);
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
            setItemName(item, value);
            itemList.add(item);
            // 移步至当前复制的元素
            selectItemIndex = itemList.size() - 1;
            initItem();
        });
    }

    /**
     * 初始化元素面板
     *
     * @param itemContentPannel 父面板
     * @param item      元素对象
     */
    protected abstract void initItemContentPanel(JPanel itemContentPannel, E item);

    /**
     * 获取元素名称
     *
     * @param item 元素对象
     * @return 元素名称
     */
    protected abstract String getItemName(E item);

    /**
     * 设置元素名称
     *
     * @param item     元素对象
     * @param itemName 元素名称
     */
    protected abstract void setItemName(E item, String itemName);

    /**
     * 创建元素
     *
     * @param name 元素名称
     * @return 元素对象
     */
    protected abstract E createItem(String name);
}
