package runnable;

import com.intellij.openapi.application.ApplicationManager;
import dialog.CompileCheckDialog;
import org.apache.http.util.TextUtils;
import utils.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 读取文件内容
 * Created by cabe on 17/1/5.
 */
public class FileRunnable implements Runnable {
    private Map<String, String> extMap = new HashMap<>();
    private String modulePath;

    public FileRunnable(String path) {
        modulePath = path;
    }

    @Override
    public void run() {
        if(TextUtils.isEmpty(modulePath)) {
            Logger.info("file path is none");
            return;
        }

        String gradleFile = modulePath + "/build.gradle";
        File file = new File(gradleFile);
        if(file.exists()) {
            Map<String, List<String>> map = readModuleGradle(gradleFile);
            showResult(map);
        } else {
            Logger.info("this folder is not an android project");
        }
    }

    private void showResult(final Map<String, List<String>> map) {
        ApplicationManager.getApplication().invokeLater(() -> {
            Logger.info("compile list is " + (map == null ? "null" : "" + map.size()));
            int maxLineLen = 0;
            String result = "";
            if(map != null && !map.isEmpty()) {
                Set<String> keySet = map.keySet();
                for(String key : keySet) {
                    List<String> list = map.get(key);
                    result += key + "\n";
                    if(key.length() > maxLineLen) {
                        maxLineLen = key.length();
                    }
                    if(list != null && !list.isEmpty()) {
                        for(String compile : list) {
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

    private String getRootGradle(String path) {
        File folder = new File(path);
        String rootPath = folder.getParent();
        return rootPath + "/build.gradle";
    }

    private String getSdkVersion(String key) {
        if(extMap.isEmpty()) {
            readExtConfig(getRootGradle(modulePath));
        }
        String version = extMap.get(key);
        return version == null ? "" : version;
    }

    private Map<String, List<String>> readModuleGradle(String gradlePath) {
        Map<String, List<String>> moduleCompile = new HashMap<>();
        try {
            FileReader fr =  new FileReader(gradlePath);
            BufferedReader br = new BufferedReader(fr);
            String str;

            Logger.info("file read start>>>>>>>>>>>>>>>>>>>>>>");
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
                    if(str.startsWith("compile")) {
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
                            String version = getSdkVersion(versionKey);
                            str = str.substring(0, str.indexOf("$rootProject")) + version;
                        }
                        Logger.info(str);

                        List<String> compileList = CheckRunnable.getCompileList(str);
                        moduleCompile.put(str, compileList);
                    }
                }
            }
            Logger.info("file read end>>>>>>>>>>>>>>>>>>>>>>");
            fr.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return moduleCompile;
    }

    private void readExtConfig(String gradlePath) {
        try {
            FileReader fr =  new FileReader(gradlePath);
            BufferedReader br = new BufferedReader(fr);
            String str;

            Logger.info("config read start>>>>>>>>>>>>>>>>>>>>>>");
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
            Logger.info("config read end>>>>>>>>>>>>>>>>>>>>>>");
            fr.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
