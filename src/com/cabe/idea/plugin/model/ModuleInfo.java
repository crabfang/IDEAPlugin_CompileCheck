package com.cabe.idea.plugin.model;

import java.util.List;
import java.util.Map;

/**
 * Module Info
 * Created by cabe on 17/1/7.
 */
public class ModuleInfo {
    public String name;
    public Map<CompileInfo, List<CompileInfo>> compileMap;

    public boolean hasCompile(CompileInfo info) {
        if(compileMap == null) return false;

        if(compileMap.containsKey(info)) return true;

        for(CompileInfo key : compileMap.keySet()) {
            List<CompileInfo> list = compileMap.get(key);
            if(list.contains(info)) return true;
        }
        return false;
    }

    public String toString() {
        return name + "-->" + compileMap;
    }
}
