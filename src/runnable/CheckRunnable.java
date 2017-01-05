package runnable;

import com.intellij.openapi.application.ApplicationManager;
import dialog.CompileCheckDialog;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.http.util.TextUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import utils.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * Created by sky on 16/5/18.
 */
public class CheckRunnable implements Runnable {
    private static String repo4U51Snapshot = "http://192.168.2.239:8081/nexus/content/repositories/snapshots";
    private static String repo4U51Release = "http://192.168.2.239:8081/nexus/content/repositories/release";
    private static String repo4JCenter = "http://jcenter.bintray.com";

    private static DocumentBuilderFactory builderFactory;
    private String mQuery;

    public CheckRunnable(String query) {
        this.mQuery = query;
    }

    public void run() {
        List<String> compileList = getCompileList(mQuery);
        showResult(compileList);
    }

    private void showResult(final List<String> compileList) {
        ApplicationManager.getApplication().invokeLater(() -> {
            Logger.info("compile list is " + (compileList == null ? "null" : "" + compileList.size()));
            String result = "";
            int maxLineLen = 0;
            if(compileList != null && !compileList.isEmpty()) {
                for(String compile : compileList) {
                    result += compile + "\n";
                    if(compile.length() > maxLineLen) {
                        maxLineLen = compile.length();
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

    public static List<String> getCompileList(String mQuery) {
        builderFactory = DocumentBuilderFactory.newInstance();
        List<String> urlList = getUrlList(mQuery);
        List<String> compileList = null;
        for(String url : urlList) {
            String response = httpGet(url);
            if(!TextUtils.isEmpty(response)) {
                compileList = parseXml4Pom(response);
                break;
            }
        }
        builderFactory = null;
        return compileList;
    }

    private static List<String> parseXml4Pom(String xmlStr) {
        List<String> compileList = new ArrayList<>();
        Document document;
        try {
            StringReader sr = new StringReader(xmlStr);
            InputSource is = new InputSource(sr);

            //DOM parser instance
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            //parse an XML file into a DOM tree
            document = builder.parse(is);

            NodeList nodeList = document.getElementsByTagName("dependency");
            for(int i=0;i<nodeList.getLength();i++) {
                Node node = nodeList.item(i);
                NodeList childNodes = node.getChildNodes();
                String[] group = new String[4];
                for(int j=0;j<childNodes.getLength();j++) {
                    Node child = childNodes.item(j);
                    String key = child.getNodeName();
                    String val = child.getTextContent();
                    switch (key) {
                        case "scope":
                            group[0] = val;
                            break;
                        case "groupId":
                            group[1] = val;
                            break;
                        case "artifactId":
                            group[2] = val;
                            break;
                        case "version":
                            group[3] = val;
                            break;
                    }
                }
                compileList.add(group[0] + " " + group[1] + ":" + group[2] + ":" + group[3]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Collections.sort(compileList);
        return compileList;
    }

    private static String parseXml4Version(String xmlStr) {
        String versionPom = "";
        Document document;
        try {
            xmlStr = xmlStr.replace("\n", "");
            xmlStr = xmlStr.replace("&nbsp;", "");

            int headStart = xmlStr.indexOf("<head>");
            int headEnd = xmlStr.indexOf("</head>") + "</head>".length();
            if(headStart >= 0 && headEnd > headStart) {
                String headStr = xmlStr.substring(headStart, headEnd);
                xmlStr = xmlStr.replace(headStr, "");
            }

            StringReader sr = new StringReader(xmlStr);
            InputSource is = new InputSource(sr);

            //DOM parser instance
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            //parse an XML file into a DOM tree
            document = builder.parse(is);

            NodeList nodeList = document.getElementsByTagName("a");
            for(int i=0;i<nodeList.getLength();i++) {
                Node node = nodeList.item(i);
                String text = node.getTextContent();
                if(text.endsWith("pom")) {
                    versionPom = text;
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return versionPom;
    }

    private static String httpGet(String url) {
        String responseStr = "";
        try {
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

    private static List<String> getUrlList(String aarInfo) {
        List<String> list = new ArrayList<>();

        list.add(createUrl(repo4U51Snapshot, aarInfo));
        list.add(createUrl(repo4U51Release, aarInfo));
        list.add(createUrl(repo4JCenter, aarInfo));

        return list;
    }

    private static String getVersionPom(String url) {
        String response = httpGet(url);
        if(TextUtils.isEmpty(response)) {
            return "";
        }
        return parseXml4Version(response);
    }

    private static String createUrl(String url, String aarInfo) {
        String[] aarArray = aarInfo.split(":");
        String groupId = aarArray[0];
        String artifactId = aarArray[1];
        String version = aarArray[2];
        for(String item : groupId.split("\\.")) {
            url += "/" + item;
        }
        url += "/" + artifactId + "/" + version + "/";
        if(!aarInfo.contains("SNAPSHOT")) {
            url += String.format("%s-%s.pom", artifactId, version);
        } else {
            url += getVersionPom(url);
        }
        return url;
    }
}
