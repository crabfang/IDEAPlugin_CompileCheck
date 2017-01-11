package com.cabe.idea.plugin.runnable;

import com.cabe.idea.plugin.dialog.CompileCheckDialog;
import com.cabe.idea.plugin.model.CompileInfo;
import com.cabe.idea.plugin.model.ModuleInfo;
import com.cabe.idea.plugin.model.PomInfo;
import com.cabe.idea.plugin.setting.SettingForm;
import com.cabe.idea.plugin.utils.Logger;
import com.cabe.idea.plugin.utils.ProjectUtils;
import com.cabe.idea.plugin.utils.XmlUtils;
import org.apache.http.util.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 搜索本地compile文件的pom配置信息
 * Created by cabe on 17/1/5.
 */
public class SearchRunnable implements Runnable {
    private String projectPath;
    private CompileInfo curInfo;

    public SearchRunnable(String projectPath, CompileInfo curInfo) {
        this.projectPath = projectPath;
        this.curInfo = curInfo;
    }

    @Override
    public void run() {
        XmlUtils.init();

//        List<PomInfo> containList = searchPom(cachePath);
//        List<CompileInfo> compileList = new ArrayList<>();
//        if(containList != null) {
//            for(PomInfo pom : containList) {
//                compileList.add(pom.info);
//                Logger.info("" + pom);
//            }
//        }

        getFilters();
        resultTips = "";
        List<ModuleInfo> moduleList = findModuleCompile(projectPath);
        if(moduleList != null) {
            for(ModuleInfo module : moduleList) {
                Map<CompileInfo, List<CompileInfo>> dependencyMap = module.compileMap;
                if(dependencyMap == null) continue;

                Logger.info("cur module : " + module.name + ">>>>>>>>>>>>>>>>>>>>>>>>>>>");
                curModule = module.name;
                for(CompileInfo compile : dependencyMap.keySet()) {
                    if(compile != null && compile.artifact.equals("RxCache")) {
                        Logger.error("compile:" + compile);
                    }
                    List<CompileInfo> relationList = new LinkedList<>();
                    traverseCompile(compile, 0, relationList);
                }
            }
        }
        Logger.info("＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝");
        Logger.info("＝＝＝＝＝＝＝＝＝＝＝＝＝＝  result  ＝＝＝＝＝＝＝＝＝＝＝＝＝");
        Logger.info("＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝");

        CompileCheckDialog dialog = new CompileCheckDialog();
        dialog.updateDialogWidth(maxLineLen);
        dialog.setLabel(resultTips);
        dialog.setVisible(true);

        XmlUtils.release();
    }

    private List<ModuleInfo> findModuleCompile(String projectPath) {
        List<ModuleInfo> moduleList = null;

        File project = new File(projectPath);
        if(project.exists()) {
            File[] childFiles = project.listFiles();
            if(childFiles != null) {
                for(File file : childFiles) {
                    String modulePath = file.getPath();
                    if(ProjectUtils.isModule(modulePath)) {
                        if(moduleList == null) {
                            moduleList = new ArrayList<>();
                        }
                        Map<CompileInfo, List<CompileInfo>> map = ProjectUtils.readModuleGradle(modulePath);

                        ModuleInfo info = new ModuleInfo();
                        info.name = file.getName();
                        info.compileMap = map;
                        moduleList.add(info);
                    }
                }
            }
        }
        return moduleList;
    }

    private void getFilters() {
        String filterConfig = SettingForm.getLocalFilter();
        if(!TextUtils.isEmpty(filterConfig)) {
            filters = filterConfig.split(",");
        }
    }

    private boolean isFilter(String str) {
        if(filters != null) {
            for(String f : filters) {
                if(str.contains(f)) {
                   return true;
                }
            }
        }
        return false;
    }

    private int maxLineLen = 0;
    private String resultTips = "";
    private String curModule = "";
    private String[] filters = null;
    private boolean traverseCompile(CompileInfo info, int traverseLevel, List<CompileInfo> relationList) {
        int maxLevel = SettingForm.getLocalLevel();
        if(traverseLevel > maxLevel || info == null) return false;

        Logger.info("cur traverse level : " + traverseLevel + " ------> " + info);

        relationList.add(info);
        if(info.equals(curInfo)) return true;

        if(isFilter(info.toString())) return false;

        boolean isContainer = false;
        List<CompileInfo> compileList = CheckRunnable.getCompileList(info.toString());
        if(compileList != null) {
            int newLevel = traverseLevel + 1;
            for(CompileInfo item : compileList) {
                List<CompileInfo> tmpList = new LinkedList<>(relationList);
                isContainer = traverseCompile(item, newLevel, tmpList);

                if(tmpList.contains(curInfo)) {
                    String relationStr = curModule + "\n";
                    for(int i=0;i<tmpList.size();i++) {
                        String lineStr = "";
                        CompileInfo ii = tmpList.get(i);
                        for(int j=0;j<i;j++) {
                            lineStr += "     ";
                        }
                        lineStr += "------>" + ii;
                        if(lineStr.length() > maxLineLen) {
                            maxLineLen = lineStr.length();
                        }
                        relationStr += lineStr + "\n";
                    }
                    Logger.info("relation : \n" + relationStr);
                    resultTips += relationStr;
                }
                if(tmpList.size() > relationList.size()) {
                    tmpList.remove(tmpList.size() - 1);
                }
            }
        }
        return isContainer;
    }

    private List<PomInfo> searchPom(String cachePath) {
        File cacheFolder = new File(cachePath);
        if(!cacheFolder.exists() || !cacheFolder.isDirectory()) return null;

        return traverseFolder(cacheFolder);
    }

    private List<PomInfo> traverseFolder(File file) {
        List<PomInfo> pomList = new ArrayList<>();

        if(file == null) return pomList;

        if(file.isDirectory()) {
            File[] childList = file.listFiles();
            if(childList != null) {
                for(File child : childList) {
                    pomList.addAll(traverseFolder(child));
                }
            }
        } else {
            String name = file.getName();
            if(name.endsWith("pom")) {
                PomInfo pomInfo = XmlUtils.parsePom4All(file);
                if(pomInfo != null) {
                    if(pomInfo.dependency(curInfo)) {
                        pomList.add(pomInfo);
                    }
                    Logger.info("" + pomInfo.info);
                }
            }
        }
        return pomList;
    }
}
