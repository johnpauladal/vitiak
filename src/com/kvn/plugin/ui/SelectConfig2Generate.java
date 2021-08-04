package com.kvn.plugin.ui;

import com.baomidou.mybatisplus.enums.IdType;
import com.baomidou.mybatisplus.generator.config.GlobalConfig;
import com.baomidou.mybatisplus.generator.config.PackageConfig;
import com.baomidou.mybatisplus.generator.config.StrategyConfig;
import com.baomidou.mybatisplus.generator.config.TemplateConfig;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.intellij.database.psi.DbTable;
import com.intellij.ide.util.PackageChooserDialog;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiPackage;
import com.kvn.plugin.CustomAutoGenerator;
import com.kvn.plugin.KvnPluginContext;
import com.kvn.plugin.config.Template;
import com.kvn.plugin.presistentConfig.PersistentConfig;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by wangzhiyuan on 2018/8/2
 */
public class SelectConfig2Generate extends JDialog {
    /**
     * 打开窗口
     */
    public void open() {
        this.pack();
        setLocationRelativeTo(null);
        this.setVisible(true);
    }

    /**
     * 主面板
     */
    private JPanel contentPane;
    /**
     * 模块下拉框
     */
    private JComboBox<String> moduleComboBox;
    private JTextField packageTextField;
    private JTextField pathTextField;
    /**
     * 包选择按钮
     */
    private JButton packageChooseButton;
    /**
     * 路径选择按钮
     */
    private JButton pathChooseButton;
    /**
     * 模板面板
     */
    private JPanel templatePanel;
    /**
     * 模板全选框
     */
    private JCheckBox allCheckBox;
    private JButton okButton;
    private JButton cancelButton;

    /**
     * 所有模板复选框
     */
    private List<JCheckBox> checkBoxList = new ArrayList<>();


    public SelectConfig2Generate() {
        initUi();
    }


    /**
     * 控件内容初始化
     */
    private void initUi() {
        initModuleUi();
        initPackageUi();
        initPathUi();
        initTemplateUi();
        initButtonUi();
        // 填充内容
        this.setContentPane(contentPane);
        this.setModal(true);
        this.getRootPane().setDefaultButton(okButton);
    }

    private void initButtonUi() {
        // 设置OK、Cancel事件
        okButton.addActionListener(e -> onOK());
        cancelButton.addActionListener(e -> onCancel());
        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });
        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void initTemplateUi() {
        //添加模板组
        checkBoxList.clear();
        templatePanel.setLayout(new GridLayout(6, 2));
        PersistentConfig.instance().getSelectedTemplateGroup().getElementList().forEach(template -> {
            JCheckBox checkBox = new JCheckBox(template.getName());
            checkBoxList.add(checkBox);
            templatePanel.add(checkBox);
        });

        //添加全选事件
        allCheckBox.addActionListener(e -> checkBoxList.forEach(jCheckBox -> jCheckBox.setSelected(allCheckBox.isSelected())));
    }

    private void initPathUi() {
        //初始化路径
        refreshPath();
        //选择路径
        pathChooseButton.addActionListener(e -> {
            //将当前选中的model设置为基础路径
            VirtualFile path = KvnPluginContext.instance().getProject().getBaseDir();
            Module module = getSelectModule();
            if (module!=null) {
                path = VirtualFileManager.getInstance().findFileByUrl("file://" + new File(module.getModuleFilePath()).getParent());
            }
            VirtualFile virtualFile = FileChooser.chooseFile(FileChooserDescriptorFactory.createSingleFolderDescriptor(), KvnPluginContext.instance().getProject(), path);
            if (virtualFile != null) {
                pathTextField.setText(virtualFile.getPath());
            }
        });
    }

    private void initPackageUi() {
        //添加包选择事件
        packageChooseButton.addActionListener(e -> {
            PackageChooserDialog dialog = new PackageChooserDialog("Package Chooser", KvnPluginContext.instance().getProject());
            dialog.show();
            PsiPackage psiPackage = dialog.getSelectedPackage();
            if (psiPackage != null) {
                packageTextField.setText(psiPackage.getQualifiedName());
                // 刷新路径
                refreshPath();
            }
        });
    }

    private void initModuleUi() {
        // 初始化Module选择
        for (Module module : KvnPluginContext.instance().getModules()) {
            moduleComboBox.addItem(module.getName());
        }
        //监听module选择事件
        moduleComboBox.addActionListener(e -> {
            // 刷新路径
            refreshPath();
        });
    }

    /**
     * 取消按钮回调事件
     */
    private void onCancel() {
        dispose();
    }

    /**
     * 确认按钮回调事件
     */
    private void onOK() {
        // FIXME 根据用户选择来生成文件
        List<Template> selectTemplateList = getSelectTemplate();
        // 如果选择的模板是空的
        if (selectTemplateList.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Can't Select Template!");
            return;
        }
        String savePath = pathTextField.getText();
        if (StringUtils.isEmpty(savePath)) {
            JOptionPane.showMessageDialog(null, "Can't Select Save Path!");
            return;
        }
        // 设置好配置信息
        KvnPluginContext.initBeforeGenerate(savePath, selectTemplateList, packageTextField.getText(), getSelectModule());

        // 生成代码
        generateCode(selectTemplateList);
        // 关闭窗口
        dispose();
    }


    /**
     * 根据用户的选择来生成代码
     * @param selectTemplateList 需要按哪些模板生成代码
     */
    private void generateCode(List<Template> selectTemplateList) {
        GlobalConfig config = new GlobalConfig()
                .setActiveRecord(true)
                .setAuthor(PersistentConfig.instance().getAuthor())
                .setOutputDir(KvnPluginContext.instance().getSavePath())
                .setIdType(IdType.AUTO)
                .setFileOverride(true);

        StrategyConfig strategyConfig = new StrategyConfig();
        String tableNames = Lists.transform(KvnPluginContext.instance().getDbTableList(), new Function<DbTable, String>() {
            public String apply(DbTable dbTable) {
                return dbTable.getName();
            }
        }).stream().collect(Collectors.joining(","));
        strategyConfig
                .setCapitalMode(true)
                .setEntityLombokModel(true) // lombok支持
                .setDbColumnUnderline(true)
                .setNaming(NamingStrategy.underline_to_camel)
//                .setTablePrefix("ts_")
                .setInclude(tableNames);//修改替换成你需要的表名，多个表名传数组

        PackageConfig packageConfig = new PackageConfig()
                .setParent(KvnPluginContext.instance().getPackageName())
                .setModuleName(null)
                .setController("controller")
                .setEntity("entity");

        // 当前使用的代码生成模板
        TemplateConfig template = new TemplateConfig(PersistentConfig.instance().getCurrTemplateGroupName(), selectTemplateList);
//        Debugger.debug(template);
        new CustomAutoGenerator().setTemplate(template).setGlobalConfig(config).setStrategy(strategyConfig).setPackageInfo(packageConfig).setDbTables(KvnPluginContext.instance().getDbTableList()).execute();
    }




    /**
     * 获取已经选中的模板
     *
     * @return 模板对象集合
     */
    private List<Template> getSelectTemplate() {
        // 获取到已选择的复选框
        List<String> selectTemplateNameList = new ArrayList<>();
        checkBoxList.forEach(jCheckBox -> {
            if (jCheckBox.isSelected()) {
                selectTemplateNameList.add(jCheckBox.getText());
            }
        });
        List<Template> selectTemplateList = new ArrayList<>(selectTemplateNameList.size());
        if (selectTemplateNameList.isEmpty()) {
            return selectTemplateList;
        }
        // 将复选框转换成对应的模板对象
        PersistentConfig.instance().getSelectedTemplateGroup().getElementList().forEach(template -> {
            if (selectTemplateNameList.contains(template.getName())) {
                selectTemplateList.add(template);
            }
        });
        return selectTemplateList;
    }


    /**
     * 刷新目录
     */
    private void refreshPath() {
        String packageName = packageTextField.getText();
        // 获取基本路径
        String path = getBasePath();
        // 兼容Linux路径
        path = path.replaceAll("\\\\", "/");
        // 如果存在包路径，添加包路径
//        if (!StringUtils.isEmpty(packageName)) {
//            path += "/" + packageName.replaceAll("\\.", "/");
//        }
        pathTextField.setText(path);
    }

    /**
     * 获取基本路径
     * @return 基本路径
     */
    private String getBasePath() {
        Module module = getSelectModule();
        String baseDir = KvnPluginContext.instance().getProject().getBasePath();
        if (module!=null) {
            baseDir = new File(module.getModuleFilePath()).getParent();
        }
        // 针对Maven项目
        File file = new File(baseDir + "/src/main/java");
        if (file.exists()) {
            return file.getAbsolutePath();
        }
        // 针对普通Java项目
        file = new File(baseDir + "/src");
        if (file.exists()) {
            return file.getAbsolutePath();
        }
        return baseDir;
    }

    /**
     * 获取选中的Module
     *
     * @return 选中的Module
     */
    private Module getSelectModule() {
        String name = (String) moduleComboBox.getSelectedItem();
        for (Module module : KvnPluginContext.instance().getModules()) {
            if (module.getName().equals(name)) {
                return module;
            }
        }
        return KvnPluginContext.instance().getModules()[0];
    }
}
