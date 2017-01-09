package com.cabe.idea.plugin.runnable;

import com.cabe.idea.plugin.dialog.CompileCheckDialog;
import com.cabe.idea.plugin.model.CompileInfo;
import com.cabe.idea.plugin.model.ModuleInfo;
import com.cabe.idea.plugin.model.PomInfo;
import com.cabe.idea.plugin.utils.Logger;
import com.cabe.idea.plugin.utils.ProjectUtils;
import com.cabe.idea.plugin.utils.XmlUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 搜索本地compile文件的pom配置信息
 * Created by cabe on 17/1/5.
 */
public class SearchRunnable implements Runnable {
    private String projectPath;
    private String cachePath;
    private CompileInfo info;

    public SearchRunnable(String projectPath, String cachePath, CompileInfo info) {
        this.projectPath = projectPath;
        this.cachePath = cachePath;
        this.info = info;
    }

    @Override
    public void run() {
        XmlUtils.init();

        List<PomInfo> containList = searchPom(cachePath);

        List<ModuleInfo> moduleList = findModuleCompile(projectPath);

        List<CompileInfo> compileList = new ArrayList<>();
        List<String> resultList = new ArrayList<>();
        if(containList != null) {
            for(PomInfo pom : containList) {
                compileList.add(pom.info);
                Logger.info("" + pom);
            }
        }
        if(moduleList != null) {
            String prefix1 = "\n  ------> ";
            String prefix2 = "\n    ------> ";
            String prefix3 = "\n      ------> ";
            for(ModuleInfo module : moduleList) {
                Map<CompileInfo, List<CompileInfo>> dependencyMap = module.compileMap;
                if(dependencyMap == null) continue;

                if(dependencyMap.containsKey(info)) {
                    resultList.add(module.name + prefix1 + info);
                }
                for(CompileInfo first : dependencyMap.keySet()) {
                    List<CompileInfo> secondCompile = dependencyMap.get(first);
                    if(secondCompile == null) continue;

                    if(secondCompile.contains(info)) {
                        resultList.add(module.name + prefix1 + first + prefix2 + info);
                    }
                    for(CompileInfo second : secondCompile) {
                        if(compileList.contains(second)) {
                            resultList.add(module.name + prefix1 + first + prefix2 + second + prefix3 + info);
                        }
                    }
                }
            }
        }
        Logger.info("＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝");
        Logger.info("＝＝＝＝＝＝＝＝＝＝＝＝＝＝  result ＝＝＝＝＝＝＝＝＝＝＝＝＝＝");
        Logger.info("＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝");
        for(String result : resultList) {
            Logger.info(result);
        }

        CompileCheckDialog dialog = new CompileCheckDialog();
        dialog.setLabel(resultList);
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
                    if(pomInfo.dependency(info)) {
                        pomList.add(pomInfo);
                    }
                    Logger.info("" + pomInfo.info);
                }
            }
        }
        return pomList;
    }
}
