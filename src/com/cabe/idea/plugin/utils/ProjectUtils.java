package com.cabe.idea.plugin.utils;

import com.cabe.idea.plugin.model.CompileInfo;
import com.cabe.idea.plugin.runnable.CheckRunnable;
import org.apache.http.util.TextUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Project Utils
 * Created by cabe on 17/1/7.
 */
public class ProjectUtils {
    private static Map<String, String> extMap = new LinkedHashMap<>();

    public static boolean isModule(String path) {
        String gradleFile = path + "/build.gradle";
        try {
            File file = new File(gradleFile);
            return file.exists();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static String getRootGradle(String path) {
        File folder = new File(path);
        String rootPath = folder.getParent();
        return rootPath + "/build.gradle";
    }

    private static String getSdkVersion(String modulePath, String key) {
        if(extMap.isEmpty()) {
            extMap = readExtConfig(getRootGradle(modulePath));
        }
        String version = extMap.get(key);
        return version == null ? "" : version;
    }

    public static Map<CompileInfo, List<CompileInfo>> readModuleGradle(String modulePath) {
        Map<CompileInfo, List<CompileInfo>> moduleCompile = new LinkedHashMap<>();
        try {
            FileReader fr =  new FileReader(modulePath + "/build.gradle");
            BufferedReader br = new BufferedReader(fr);
            String str;

//            Logger.info("file read start>>>>>>>>>>>>>>>>>>>>>>");
            boolean inDependency = false;
            while ((str = br.readLine() )!=null) {
                if(str.startsWith("dependencies")) {
                    inDependency = true;
                    continue;
                } else if (str.startsWith("}")) {
                    inDependency = false;
                    continue;
                }
                if(inDependency) {
                    str = str.trim();
                    if(str.startsWith("compile") || str.startsWith("androidTestCompile") || str.startsWith("testCompile")
                            || str.startsWith("debugCompile") || str.startsWith("releaseCompile")) {
                        if(str.contains("fileTree") || str.contains("project")) {
                            continue;
                        }
                        if(str.contains("\"")) {
                            str = str.split("\"")[1];
                        } else if(str.contains("'")) {
                            str = str.split("'")[1];
                        }
                        if(str.contains("$rootProject")) {
                            String versionStr = str.substring(str.indexOf("$rootProject"));
                            String[] vArray = versionStr.split("\\.");
                            String versionKey = vArray[vArray.length - 1];
                            String version = getSdkVersion(modulePath, versionKey);
                            str = str.substring(0, str.indexOf("$rootProject")) + version;
                        }
                        Logger.info(str);

                        List<CompileInfo> compileList = CheckRunnable.getCompileList(str);
                        moduleCompile.put(CompileInfo.parseCompile(str), compileList);
                    }
                }
            }
//            Logger.info("file read end>>>>>>>>>>>>>>>>>>>>>>");
            fr.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return moduleCompile;
    }

    private static Map<String, String> readExtConfig(String gradlePath) {
        Map<String, String> extMap = new LinkedHashMap<>();
        try {
            FileReader fr =  new FileReader(gradlePath);
            BufferedReader br = new BufferedReader(fr);
            String str;

//            Logger.info("config read start>>>>>>>>>>>>>>>>>>>>>>");
            boolean inExt = false;
            while ((str = br.readLine() )!=null) {
                if(TextUtils.isEmpty(str)) continue;

                if(str.startsWith("ext")) {
                    inExt = true;
                    continue;
                } else if (str.startsWith("}")) {
                    inExt = false;
                    continue;
                }
                if(inExt) {
                    str = str.trim();
                    if(str.contains("=") && !str.startsWith("//")) {
                        Logger.info(str);
                        String[] group = str.split("=");
                        String key = group[0].trim();
                        String val = group[1].trim();
                        val = val.substring(1, val.length() - 1);
                        extMap.put(key, val);
                    }
                }
            }
//            Logger.info("config read end>>>>>>>>>>>>>>>>>>>>>>");
            fr.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return extMap;
    }
}
