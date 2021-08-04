package com.kvn.plugin.action;

import com.intellij.database.psi.DbTable;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.kvn.plugin.KvnPluginContext;
import com.kvn.plugin.ui.SelectConfig2Generate;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangzhiyuan on 2018/8/2
 */
public class PluginMainAction extends AnAction {

    /**
     * action打开面板后的处理逻辑
     * @param event 事件对象
     */
    @Override
    public void actionPerformed(AnActionEvent event) {
        initKvnPluginContext(event);
        // 打开对话框
        new SelectConfig2Generate().open();
    }

    /**
     * 初始化上下文数据
     * @param event
     */
    private void initKvnPluginContext(AnActionEvent event) {
        // 获取当前项目
        Project project = getEventProject(event);
        if (project == null) {
            return;
        }

        //获取模块
        Module[] modules = ModuleManager.getInstance(project).getModules();

        //获取选中的单个表
        PsiElement psiElement = event.getData(LangDataKeys.PSI_ELEMENT);
        DbTable selectDbTable = null;
        if (psiElement instanceof DbTable) {
            selectDbTable = (DbTable) psiElement;
        }
        if (selectDbTable == null) {
            return;
        }

        // 获取选中的所有表
        PsiElement[] psiElements = event.getData(LangDataKeys.PSI_ELEMENT_ARRAY);
        if (psiElements == null || psiElements.length == 0) {
            return;
        }
        List<DbTable> dbTableList = new ArrayList<>();
        for (PsiElement element : psiElements) {
            if (!(element instanceof DbTable)) {
                continue;
            }
            DbTable dbTable = (DbTable) element;
            dbTableList.add(dbTable);
        }
        if (dbTableList.isEmpty()) {
            return;
        }

        // 初始化上下文
        KvnPluginContext.initAfterPluginPop(dbTableList, modules, project, selectDbTable);

        System.out.println("initKvnPluginContext success!");
    }
}
