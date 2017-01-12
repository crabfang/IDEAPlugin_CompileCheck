package com.cabe.idea.plugin.runnable;

import com.cabe.idea.plugin.dialog.CompileCheckDialog;
import com.cabe.idea.plugin.model.CompileInfo;
import com.cabe.idea.plugin.model.MetaData;
import com.cabe.idea.plugin.setting.SettingForm;
import com.cabe.idea.plugin.utils.CommonUtils;
import com.cabe.idea.plugin.utils.Logger;
import com.cabe.idea.plugin.utils.PomUtils;
import com.cabe.idea.plugin.utils.XmlUtils;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.projectRoots.Sdk;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.http.util.TextUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * Created by sky on 16/5/18.
 */
public class CheckRunnable implements Runnable {
    private static String suffixSnapshot = "/snapshots";
    private static String suffixRelease = "/release";
    private static String repo4JCenter = "http://jcenter.bintray.com";

    private String aarInfo;

    public CheckRunnable(String aarInfo) {
        this.aarInfo = aarInfo;
    }

    public void run() {
        XmlUtils.init();
        List<CompileInfo> compileList = getCompileList(aarInfo);
        showResult(compileList);
        XmlUtils.release();
    }

    private void showResult(final List<CompileInfo> compileList) {
        ApplicationManager.getApplication().invokeLater(() -> {
            Logger.info("compile list is " + (compileList == null ? "null" : "" + compileList.size()));
            String result = "";
            int maxLineLen = 0;
            if(compileList != null && !compileList.isEmpty()) {
                for(CompileInfo compile : compileList) {
                    String str = compile.toString();
                    result += str + "\n";
                    if(str.length() > maxLineLen) {
                        maxLineLen = str.length();
                    }
                }
            } else {
                result += "check no result";
            }

            CompileCheckDialog dialog = new CompileCheckDialog();
            dialog.updateDialogWidth(maxLineLen);
            dialog.setLabel(result);
            dialog.setVisible(true);
        });
    }

    private static boolean isUnsteadinessVersion(String aarInfo) {
        return aarInfo.contains("+") || aarInfo.contains("latest");
    }

    public static List<CompileInfo> getCompileList(String aarInfo) {
        long deltaTime = System.currentTimeMillis();

        if(aarInfo.endsWith("null")) {
            aarInfo = aarInfo.replace("null", "+");
        }
        if(isUnsteadinessVersion(aarInfo)) {
            List<String> metadata = getMetadataUrlList(aarInfo);
            MetaData data = null;
            for(String url : metadata) {
                MetaData item = getLastVersion(url);
                if(item != null) {
                    if(data == null) {
                        data = item;
                    } else {
                        if(data.lastTime.compareTo(item.lastTime) > 0) {
                            data = item;
                        }
                    }
                }
            }
            if(data != null && !TextUtils.isEmpty(data.release)) {
                aarInfo = aarInfo.replace("+", data.release).replace("latest", data.release);
            }
        }
        List<CompileInfo> compileList = null;
        String pomCache = PomUtils.createPomFilePath(aarInfo);
        File cache = new File(pomCache);
        if(cache.exists()) {
            compileList = XmlUtils.parsePom4DependencyWithFile(pomCache);
        } else {
            if(aarInfo.contains("com.android")) {
                String androidPom = getLocalAndroidPomPath(aarInfo);
                compileList = XmlUtils.parsePom4DependencyWithFile(androidPom);
            } else {
                if(!aarInfo.contains("null")) {
                    List<String> urlList = getUrlList(aarInfo);
                    for(String url : urlList) {
                        String response = httpGet(url);
                        if(!TextUtils.isEmpty(response)) {
                            compileList = XmlUtils.parsePom4Dependency(response);
                            if(!isUnsteadinessVersion(aarInfo)) {
                                try {
                                    PomUtils.savePom(aarInfo, response);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }
        deltaTime = System.currentTimeMillis() - deltaTime;
        if(deltaTime > 1000) {
            Logger.info(aarInfo + " --> delta time : " + deltaTime);
        }
        return compileList;
    }

    private static String httpGet(String url) {
        String responseStr = "";
        try {
            if(url.contains("{") || url.contains("[")) {
                Logger.error("http url error:" + url);
                return "";
            }

            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(5000).setConnectTimeout(5000)
                    .setConnectionRequestTimeout(5000).build();
            HttpGet httpGet = new HttpGet(url);
            httpGet.setConfig(requestConfig);
            HttpClient client = HttpClients.createDefault();
            HttpResponse response = client.execute(httpGet);
            int status = response.getStatusLine().getStatusCode();
            if (status >= 200 && status < 300) {
                HttpEntity resEntity = response.getEntity();
                responseStr = EntityUtils.toString(resEntity, "UTF-8");
                Logger.info(url + "----> get data");
            }
        } catch (IOException e) {
            Logger.error("http request error");
        }
        return responseStr;
    }

    private static List<String> getMetadataUrlList(String aarInfo) {
        List<String> list = new ArrayList<>();

        String customPath = SettingForm.getCustomPath();
        String suffix = "/maven-metadata.xml";
        list.add(createUrl(customPath + suffixSnapshot, aarInfo, false) + suffix);
        list.add(createUrl(customPath + suffixRelease, aarInfo, false) + suffix);
        list.add(createUrl(repo4JCenter, aarInfo, false) + suffix);

        return list;
    }

    private static List<String> getUrlList(String aarInfo) {
        List<String> list = new ArrayList<>();

        String customPath = SettingForm.getCustomPath();
        if(aarInfo.contains("SNAPSHOT")) {
            list.add(createUrl(customPath + suffixSnapshot, aarInfo, true));
        } else {
            list.add(createUrl(customPath + suffixRelease, aarInfo, true));
        }
        list.add(createUrl(repo4JCenter, aarInfo, true));

        return list;
    }

    private static MetaData getLastVersion(String url) {
        String response = httpGet(url);
        if(TextUtils.isEmpty(response)) {
            return null;
        }
        return XmlUtils.parseXml4Metadata(response);
    }

    private static String getVersionPom(String url) {
        String response = httpGet(url);
        if(TextUtils.isEmpty(response)) {
            return "";
        }
        return XmlUtils.parseHtml4Version(response);
    }

    private static String createUrl(String url, String aarInfo, boolean containerVersion) {
        String[] aarArray = aarInfo.split(":");
        if(aarArray.length > 2) {
            String groupId = aarArray[0];
            String artifactId = aarArray[1];
            String version = aarArray[2];
            for(String item : groupId.split("\\.")) {
                url += "/" + item;
            }
            url += "/" + artifactId;
            if(containerVersion) {
                url += "/" + version + "/";
                if(!aarInfo.contains("SNAPSHOT")) {
                    url += String.format("%s-%s.pom", artifactId, version);
                } else {
                    url += getVersionPom(url);
                }
            }
        }
        return url;
    }

    private static String getLocalAndroidPomPath(String aarInfo) {
        if(TextUtils.isEmpty(aarInfo)) return null;

        String pom = "";
        Sdk sdk = CommonUtils.findAndroidSDK();
        if(sdk != null) {
            pom = sdk.getHomePath();
            pom += "/extras/android/m2repository";
        }
        return pom + parseAarInfo2Path(aarInfo);
    }

    private static String parseAarInfo2Path(String aarInfo) {
        if(TextUtils.isEmpty(aarInfo)) return "";

        String path = "";
        String[] aarArray = aarInfo.split(":");
        if(aarArray.length > 2) {
            String groupId = aarArray[0];
            String artifactId = aarArray[1];
            String version = aarArray[2];
            for(String item : groupId.split("\\.")) {
                path += "/" + item;
            }
            path += "/" + artifactId + "/" + version + "/";
            path += String.format("%s-%s.pom", artifactId, version);
        }
        return path;
    }
}
