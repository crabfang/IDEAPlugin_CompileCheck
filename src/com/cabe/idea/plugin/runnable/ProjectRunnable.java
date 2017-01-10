package com.cabe.idea.plugin.runnable;

import com.cabe.idea.plugin.dialog.CompileCheckDialog;
import com.cabe.idea.plugin.model.CompileInfo;
import com.cabe.idea.plugin.utils.Logger;
import com.cabe.idea.plugin.utils.ProjectUtils;
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

    public ProjectRunnable(String path) {
        modulePath = path;
    }

    @Override
    public void run() {
        if(TextUtils.isEmpty(modulePath)) {
            String tips = "file path is none";
            Logger.info(tips);

            CompileCheckDialog dialog = new CompileCheckDialog();
            dialog.setLabel(tips);
            dialog.setVisible(true);
            return;
        }


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
    }

    private void showResult(final Map<CompileInfo, List<CompileInfo>> map) {
        ApplicationManager.getApplication().invokeLater(() -> {
            Logger.info("compile list is " + (map == null ? "null" : "" + map.size()));
            int maxLineLen = 0;
            String result = "";
            if(map != null && !map.isEmpty()) {
                Set<CompileInfo> keySet = map.keySet();
                for(CompileInfo key : keySet) {
                    String keyStr = key.toString();
                    List<CompileInfo> list = map.get(key);
                    result += key + "\n";
                    if(keyStr.length() > maxLineLen) {
                        maxLineLen = keyStr.length();
                    }
                    if(list != null && !list.isEmpty()) {
                        for(CompileInfo compile : list) {
                            result += "      " + compile + "\n";
                            if(("      " + compile).length() > maxLineLen) {
                                maxLineLen = ("      " + compile).length();
                            }
                        }
                    }
                }
            } else {
                result += "check no result\n";
            }
            Logger.info(result);

            CompileCheckDialog dialog = new CompileCheckDialog();
            dialog.updateDialogWidth(maxLineLen);
            dialog.setLabel(result);
            dialog.setVisible(true);
        });
    }
}
