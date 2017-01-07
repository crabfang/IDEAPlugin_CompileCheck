package com.cabe.idea.plugin.model;

import java.util.List;

/**
 * Pom Config Info
 * Created by cabe on 17/1/7.
 */
public class PomInfo {
    public CompileInfo info;
    public List<CompileInfo> dependency;

    public boolean dependency(CompileInfo info) {
        return dependency != null && dependency.contains(info);
    }

    public String toString() {
        String str = info + "\n";
        if(dependency != null) {
            for(CompileInfo item : dependency) {
                str += "      " + item + "\n";
            }
        }

        return str;
    }
}
