package com.cabe.idea.plugin.utils;

import com.cabe.idea.plugin.setting.SettingForm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Locale;

/**
 * Pom File Utils
 * Created by cabe on 17/1/9.
 */
public class PomUtils {

    private static String getPomFolderPath() {
        return SettingForm.getLocalCache() + "/pom";
    }

    public static String createPomFilePath(String aarInfo) {
        return getPomFolderPath() + "/" + aarInfo + ".pom";
    }

    public static void savePom(String aarInfo, String content) throws Exception {
        //SNAPSHOT版本不缓存
        if(aarInfo.toLowerCase(Locale.getDefault()).contains("snapshot")) return;

        String filePath = createPomFilePath(aarInfo);
        File file = new File(filePath);
        File folder = file.getParentFile();
        if(file.exists() || folder.mkdirs() || file.createNewFile()) {
            content = content.trim();
            if(content.startsWith("<?xml")) {
                content = content.substring(38);
            } else if(content.startsWith("<html>")) {
                content = XmlUtils.handleHtml(content);
            }
            FileWriter fw = new FileWriter(filePath);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.newLine();
            bw.write(content);
            bw.close();
            fw.close();
        }
    }
}
