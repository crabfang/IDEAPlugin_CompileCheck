package com.cabe.idea.plugin.runnable;

import com.cabe.idea.plugin.dialog.CompileCheckDialog;
import com.cabe.idea.plugin.model.CompileInfo;
import com.cabe.idea.plugin.setting.SettingForm;
import com.cabe.idea.plugin.utils.CommonUtils;
import com.cabe.idea.plugin.utils.Logger;
import com.cabe.idea.plugin.utils.ProjectUtils;
import com.cabe.idea.plugin.utils.XmlUtils;
import com.intellij.openapi.application.ApplicationManager;
import org.apache.http.util.TextUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 读取文件内容
 * Created by cabe on 17/1/5.
 */
public class ProjectRunnable implements Runnable {
    private String modulePath;
    private long deltaTime;

    public ProjectRunnable(String path) {
        modulePath = path;
    }

    @Override
    public void run() {
        XmlUtils.init();
        if(TextUtils.isEmpty(modulePath)) {
            String tips = "file path is none";
            Logger.info(tips);

            CompileCheckDialog dialog = new CompileCheckDialog();
            dialog.setLabel(tips);
            dialog.setVisible(true);
            return;
        }

        deltaTime = System.currentTimeMillis();
        if(ProjectUtils.isModule(modulePath)) {
            Map<CompileInfo, List<CompileInfo>> map = ProjectUtils.readModuleGradle(modulePath);
            showResult(map);
        } else {
            String tips = "this folder is not an android project";
            Logger.info(tips);

            CompileCheckDialog dialog = new CompileCheckDialog();
            dialog.setLabel(tips);
            dialog.setVisible(true);
        }
        XmlUtils.release();
    }

    private int maxLineLen = 0;
    private String resultTips = "";
    private void traverseCompile(CompileInfo info, int level) {
        Logger.info("traverse compile level : " + level + " ---> " + info);
        if(level > SettingForm.getCompileLevel()) return;

        String tips = "";
        tips += CommonUtils.createLevelPrefix(level) + info;
        resultTips += tips + "\n";
        if(tips.length() > maxLineLen) {
            maxLineLen = tips.length();
        }
        List<CompileInfo> list = CheckRunnable.getCompileList(info.toString());
        if(list != null) {
            int newLevel = level + 1;
            for(CompileInfo item : list) {
                traverseCompile(item, newLevel);
            }
        }
    }

    private void showResult(final Map<CompileInfo, List<CompileInfo>> map) {
        Logger.info("compile list is " + (map == null ? "null" : "" + map.size()));

        maxLineLen = 0;
        resultTips = "";
        if(map != null && !map.isEmpty()) {
            Set<CompileInfo> keySet = map.keySet();
            for(CompileInfo key : keySet) {
                traverseCompile(key, 0);
            }
        } else {
            resultTips += "check no result\n";
        }
        Logger.info(resultTips);
        deltaTime = System.currentTimeMillis() - deltaTime;
        Logger.info("elapsed time : " + deltaTime);

        ApplicationManager.getApplication().invokeLater(() -> {
            CompileCheckDialog dialog = new CompileCheckDialog();
            dialog.updateDialogWidth(maxLineLen);
            dialog.setLabel(resultTips);
            dialog.setVisible(true);
        });
    }
}
