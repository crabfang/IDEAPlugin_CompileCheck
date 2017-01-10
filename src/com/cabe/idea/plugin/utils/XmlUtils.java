package com.cabe.idea.plugin.utils;

import com.cabe.idea.plugin.model.CompileInfo;
import com.cabe.idea.plugin.model.PomInfo;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Xml File Parse
 * Created by cabe on 17/1/7.
 */
public class XmlUtils {
    private static DocumentBuilderFactory builderFactory;

    public static void init() {
        builderFactory = DocumentBuilderFactory.newInstance();
    }

    public static void release() {
        builderFactory = null;
    }

    private static List<CompileInfo> parseDependency(NodeList nodeList) {
        List<CompileInfo> dependency = null;

        if(nodeList != null && nodeList.getLength() > 0) {
            dependency = new ArrayList<>();
            for(int i=0;i<nodeList.getLength();i++) {
                Node node = nodeList.item(i);
                if(!node.getNodeName().equals("dependency")) continue;

                NodeList childNodes = node.getChildNodes();
                CompileInfo info = new CompileInfo();
                for(int j=0;j<childNodes.getLength();j++) {
                    Node child = childNodes.item(j);
                    String key = child.getNodeName();
                    String val = child.getTextContent();
                    switch (key) {
                        case "scope":
                            info.scope = val;
                            break;
                        case "groupId":
                            info.group = val;
                            break;
                        case "artifactId":
                            info.artifact = val;
                            break;
                        case "version":
                            info.version = val;
                            break;
                    }
                }
                dependency.add(info);
            }
        }
        return dependency;
    }

    public static PomInfo parsePom4All(File poxFile) {
        PomInfo info = null;
        Document document;
        try {
            //DOM parser instance
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            //parse an XML file into a DOM tree
            document = builder.parse(poxFile);

            NodeList nodeList = document.getChildNodes().item(0).getChildNodes();
            if(nodeList != null && nodeList.getLength() > 0) {
                info = new PomInfo();
                info.info = new CompileInfo();
                for(int i=0;i<nodeList.getLength();i++) {
                    Node child = nodeList.item(i);
                    String key = child.getNodeName();
                    String val = child.getTextContent();
                    switch (key) {
                        case "scope":
                            info.info.scope = val;
                            break;
                        case "groupId":
                            info.info.group = val;
                            break;
                        case "artifactId":
                            info.info.artifact = val;
                            break;
                        case "version":
                            info.info.version = val;
                            break;
                        case "dependencies":
                            info.dependency = parseDependency(child.getChildNodes());
                            break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return info;
    }

    public static String handleHtml(String html) {
        html = html.replace("\n", "");
        html = html.replace("&nbsp;", "");

        int headStart = html.indexOf("<head>");
        int headEnd = html.indexOf("</head>") + "</head>".length();
        if(headStart >= 0 && headEnd > headStart) {
            String headStr = html.substring(headStart, headEnd);
            html = html.replace(headStr, "");
        }
        return html;
    }

    public static List<CompileInfo> parsePom4DependencyWithFile(String xmlPath) {
        List<CompileInfo> compileList = new ArrayList<>();
        Document document;
        try {
            //DOM parser instance
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            //parse an XML file into a DOM tree
            document = builder.parse(xmlPath);

            NodeList nodeList = document.getElementsByTagName("dependency");
            compileList = parseDependency(nodeList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(compileList != null) {
            compileList.sort(Comparator.comparing(CompileInfo::toString));
        }
        return compileList;
    }

    public static List<CompileInfo> parsePom4Dependency(String xmlStr) {
        List<CompileInfo> compileList = new ArrayList<>();
        Document document;
        try {
            xmlStr = handleHtml(xmlStr);
            StringReader sr = new StringReader(xmlStr);
            InputSource is = new InputSource(sr);

            //DOM parser instance
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            //parse an XML file into a DOM tree
            document = builder.parse(is);

            NodeList nodeList = document.getElementsByTagName("dependency");
            compileList = parseDependency(nodeList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(compileList != null) {
            compileList.sort(Comparator.comparing(CompileInfo::toString));
        }
        return compileList;
    }

    public static String parseHtml4Version(String xmlStr) {
        String versionPom = "";
        Document document;
        try {
            xmlStr = handleHtml(xmlStr);
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
}
