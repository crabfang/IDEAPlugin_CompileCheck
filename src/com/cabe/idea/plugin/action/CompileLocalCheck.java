package com.cabe.idea.plugin.action;

import com.cabe.idea.plugin.model.CompileInfo;
import com.cabe.idea.plugin.runnable.SearchRunnable;
import com.cabe.idea.plugin.setting.SettingForm;
import com.cabe.idea.plugin.utils.CommonUtils;
import com.cabe.idea.plugin.utils.Logger;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import org.apache.http.util.TextUtils;

/**
 * Local Compile Check
 * Created by cabe on 17/1/6.
 */
public class CompileLocalCheck extends AnAction {
    @Override
    public void update(AnActionEvent event) {
        Project project = event.getProject();
        if (project == null) return;
        Navigatable element = event.getData(CommonDataKeys.NAVIGATABLE);
        if (element instanceof PsiClass) {
            PsiFile psiFile = ((PsiClass) element).getContainingFile();
            if (psiFile == null) return;
            VirtualFile virtualFile = psiFile.getVirtualFile();
            if (virtualFile == null) return;
            event.getPresentation().setEnabledAndVisible(true);
        }
    }

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

        CompileInfo info = null;
        VirtualFile virtualFile = event.getData(PlatformDataKeys.VIRTUAL_FILE);
        if(virtualFile != null) {
            String path = virtualFile.getPath();
            String tmp = "";
            if(path.contains("build/intermediates/exploded-aar")) {
                String[] group = path.split("build/intermediates/exploded-aar");
                if(group.length > 1) {
                    tmp = group[1];
                }
            } else if(path.contains("caches/modules-2/files-2.1")) {
                String[] group = path.split("caches/modules-2/files-2.1");
                if(group.length > 1) {
                    tmp = group[1];
                }
            }

            if(!TextUtils.isEmpty(tmp)) {
                String[] array = tmp.substring(1).split("/");
                if(array.length > 2) {
                    info = new CompileInfo(array[0], array[1], array[2]);
                }
            }
        }

        String cache = SettingForm.getLocalCache();
        Logger.info("compile name --> " + info + " # " + cache);
        new Thread(new SearchRunnable(projectPath, cache, info)).start();
    }
}
