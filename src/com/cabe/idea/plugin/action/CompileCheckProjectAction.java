package com.cabe.idea.plugin.action;

import com.cabe.idea.plugin.dialog.CompileCheckDialog;
import com.cabe.idea.plugin.runnable.ProjectRunnable;
import com.cabe.idea.plugin.utils.CommonUtils;
import com.cabe.idea.plugin.utils.Logger;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.File;

/**
 * Project Compile Check
 * Created by cabe on 17/1/3.
 */
public class CompileCheckProjectAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        // TODO: insert action logic heree
        if (!CommonUtils.isFastClick(1000)) {
            Logger.init(getClass().getSimpleName(), Logger.DEBUG);
            handleEvent(e);
        }
    }

    private void handleEvent(AnActionEvent event) {
        String projectPath = "";
        Project project = event.getData(PlatformDataKeys.PROJECT);
        if(project != null) {
            projectPath = project.getBasePath();
        }

        String modulePath = "";
        VirtualFile virtualFile = event.getData(PlatformDataKeys.VIRTUAL_FILE);
        if(virtualFile != null) {
            modulePath = virtualFile.getPath();
        }

        String[] group = modulePath.split("/");
        String moduleName = group[group.length - 1];
        Logger.info(moduleName + "-->" + modulePath);

        File file = new File(modulePath);
        if(file.isDirectory() && file.getParent().equals(projectPath)) {
            new Thread(new ProjectRunnable(modulePath)).start();
        } else {
            CompileCheckDialog.showDialog("this is not a module");
        }
    }
}
