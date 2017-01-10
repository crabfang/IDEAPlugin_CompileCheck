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

        List<ModuleInfo> moduleList = findModuleCompile(projectPath);

        List<String> resultList = new ArrayList<>();
        if(moduleList != null) {
            String prefix = "\n  ----> ";
            for(ModuleInfo module : moduleList) {
                Map<CompileInfo, List<CompileInfo>> dependencyMap = module.compileMap;
                if(dependencyMap == null) continue;

                if(dependencyMap.containsKey(curInfo)) {
                    resultList.add(module.name + prefix + curInfo);
                }
                for(CompileInfo compile : dependencyMap.keySet()) {
                    if(compile != null && compile.artifact.equals("RxCache")) {
                        Logger.error("compile:" + compile);
                    }
                    List<CompileInfo> dList = traverseCompile(compile, 0);
                    if(dList != null) {
                        String dependency = module.name + prefix + compile;
                        int count = 1;
                        for(CompileInfo item : dList) {
                            dependency += "\n  ";
                            for(int i=0;i<count;i++) {
                                dependency += "  ";
                            }
                            dependency += "----> ";
                            dependency += item;

                            count ++;
                        }
                        resultList.add(dependency);
                    }
                }
            }
        }
        Logger.info("＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝");
        Logger.info("＝＝＝＝＝＝＝＝＝＝＝＝＝＝  result  ＝＝＝＝＝＝＝＝＝＝＝＝＝");
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
                    if(pomInfo.dependency(curInfo)) {
                        pomList.add(pomInfo);
                    }
                    Logger.info("" + pomInfo.info);
                }
            }
        }
        return pomList;
    }

    private List<CompileInfo> traverseCompile(CompileInfo info, int traverseLevel) {
        Logger.info("cur traverse level : " + traverseLevel + " -->" + info);
        if(traverseLevel > 10) {
            return null;
        }

        List<CompileInfo> relationList = null;
        if(info != null) {
            if(info.equals(curInfo)) {
                relationList = new ArrayList<>();
                relationList.add(info);
            } else {
                List<CompileInfo> compileList = CheckRunnable.getCompileList(info.toString());
                if(compileList != null) {
                    for(CompileInfo item : compileList) {
                        if(!item.group.contains("com.squareup")) continue;

                        List<CompileInfo> list = traverseCompile(item, ++ traverseLevel);
                        if(list != null) {
                            if(relationList == null) {
                                relationList = new ArrayList<>();
                            }
                            relationList.add(item);
                            relationList.addAll(list);
                        }
                    }
                }
            }
        }
        return relationList;
    }
}
